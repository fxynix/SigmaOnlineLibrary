import React, { useState, useEffect } from 'react';
import { Button, Space, Modal, Form, Input, Select, Tag, message, Spin, InputNumber, Row, Col, Card, Tooltip, Empty } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import axios from 'axios';
import { Link } from "react-router-dom";
import 'antd/dist/reset.css';

const { Option } = Select;

const BookList = () => {
    const [books, setBooks] = useState([]);
    const [authors, setAuthors] = useState([]);
    const [categories, setCategories] = useState([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingBook, setEditingBook] = useState(null);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const[submitting, setSubmitting] = useState(false);
    const [searchText, setSearchText] = useState('');

    const currentUser = JSON.parse(localStorage.getItem('user'));
    const isAdmin = currentUser?.role === 'ADMIN';

    useEffect(() => {
        fetchBooks();
        fetchAuthors();
        fetchCategories();
    },[]);

    const fetchBooks = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/books`);
            setBooks(response.data.map(book => ({
                ...book,
                authors: Array.isArray(book.authors) ? book.authors : [],
                categories: Array.isArray(book.categories) ? book.categories :[],
            })));
        } catch (error) {
            message.error('Не удалось загрузить книги');
        } finally {
            setLoading(false);
        }
    };

    const fetchAuthors = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/authors`);
            setAuthors(response.data);
        } catch (error) {
            // message.error('Не удалось загрузить авторов');
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/categories`);
            setCategories(response.data);
        } catch (error) {
            // message.error('Не удалось загрузить жанры');
        }
    };

    const showModal = (book = null) => {
        setEditingBook(book);
        form.resetFields();
        if (book) {
            const authorIds = authors
                .filter(author => book.authors?.some(a => a === author.name || a?.name === author.name))
                .map(author => author.id);

            const categoryIds = categories
                .filter(category => book.categories?.some(c => c === category.name || c?.name === category.name))
                .map(category => category.id);

            form.setFieldsValue({
                name: book.name || '',
                authorIds,
                categoryIds,
                year: book.year || '',
                pageAmount: book.pageAmount || '',
            });
        } else {
            form.setFieldsValue({
                name: '',
                authorIds: [],
                categoryIds:[],
                year: '',
                pageAmount: '',
                info: ''
            });
        }
        setIsModalVisible(true);
    };

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const requestData = {
                name: values.name,
                authorIds: values.authorIds,
                categoryIds: values.categoryIds,
                year: values.year,
                pageAmount: values.pageAmount
            };

            if (editingBook) {
                await axios.put(
                    `${process.env.REACT_APP_API_URL}/books/${editingBook.id}`,
                    requestData
                );
                message.success('Книга отредактирована успешно');
            } else {
                await axios.post(
                    `${process.env.REACT_APP_API_URL}/books`,
                    requestData
                );
                message.success('Книга добавлена успешно');
            }

            fetchBooks();
            setIsModalVisible(false);
        } catch (error) {
            if (error.response?.status === 400) {
                const errorMessages = Object.entries(error.response.data)
                    .flatMap(([field, errors]) =>
                        Array.isArray(errors)
                            ? errors.map(e => `${field}: ${e}`)
                            : `${field}: ${errors}`
                    )
                    .join('\n');

                message.error({
                    content: <div style={{ whiteSpace: 'pre-line' }}>{errorMessages}</div>,
                    duration: 5
                });
            } else {
                message.error(error.response?.data?.message || 'Не удалось сохранить книгу');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        Modal.confirm({
            title: 'Удалить книгу',
            content: 'Вы уверены, что хотите удалить эту книгу?',
            okText: 'Удалить',
            okType: 'danger',
            cancelText: 'Отменить',
            onOk: async () => {
                try {
                    await axios.delete(`${process.env.REACT_APP_API_URL}/books/${id}`);
                    message.success('Книга успешна удалена');
                    fetchBooks();
                } catch (error) {
                    message.error('Не удалось удалить книгу');
                }
            }
        });
    };

    // Логика поиска
    const filteredBooks = books.filter(book => {
        const matchName = book.name.toLowerCase().includes(searchText.toLowerCase());
        const matchAuthor = book.authors.some(author => {
            const authorName = typeof author === 'string' ? author : author?.name;
            return authorName?.toLowerCase().includes(searchText.toLowerCase());
        });
        return matchName || matchAuthor;
    });

    return (
        <Spin spinning={loading} tip="Loading...">
            <span className="name-text">Все книги</span>
            <div className="container">
                <div className="actions" style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
                    <Input.Search
                        placeholder="Поиск по названию или автору..."
                        allowClear
                        enterButton={<SearchOutlined />}
                        size="large"
                        onChange={(e) => setSearchText(e.target.value)}
                        style={{ maxWidth: 400 }}
                    />
                    {isAdmin && (
                        <Button type="primary"
                                icon={<PlusOutlined />}
                                onClick={() => showModal()}
                                className="add-button"
                                size="large"
                        >
                            Добавить книгу
                        </Button>
                    )}
                </div>

                {filteredBooks.length === 0 ? (
                    <Empty description="Книги не найдены" style={{ marginTop: '50px' }} />
                ) : (
                    <Row gutter={[16, 16]}>
                        {filteredBooks.map(book => (
                            <Col xs={24} sm={12} md={8} lg={8} xl={6} key={book.id}>
                                <Card
                                    title={book.name}
                                    hoverable
                                    actions={isAdmin ?[
                                        <Tooltip title="Редактировать книгу">
                                            <EditOutlined key="edit" onClick={() => showModal(book)} />
                                        </Tooltip>,
                                        <Tooltip title="Удалить книгу">
                                            <DeleteOutlined key="delete" style={{ color: 'red' }} onClick={() => handleDelete(book.id)} />
                                        </Tooltip>
                                    ] :[]}
                                >
                                    <div style={{ marginBottom: '8px' }}>
                                        <strong>Авторы: </strong>
                                        {book.authors.length > 0 ? (
                                            book.authors.map((author, index) => (
                                                <Tag key={index} style={{ marginBottom: '4px' }}>
                                                    {typeof author === 'string' ? author : author?.name ?? '—'}
                                                </Tag>
                                            ))
                                        ) : '-'}
                                    </div>
                                    <div style={{ marginBottom: '8px' }}>
                                        <strong>Жанры: </strong>
                                        {book.categories.length > 0 ? (
                                            book.categories.map((category, index) => (
                                                <Tag key={index} color="blue" style={{ marginBottom: '4px' }}>
                                                    {typeof category === 'string' ? category : category?.name ?? '—'}
                                                </Tag>
                                            ))
                                        ) : '-'}
                                    </div>
                                    <p><strong>Год:</strong> {book.year || '-'}</p>
                                    <p><strong>Страниц:</strong> {book.pageAmount}</p>
                                    <p><strong>Рейтинг:</strong> {book.rating != null ? book.rating.toFixed(1) : '0.0'} / 5.0</p>
                                    <p>
                                        <strong>Отзывы:</strong> <Link to={`/books/${book.id}/reviews`}>{book.reviews?.length || 0}</Link>
                                    </p>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )}

                <Modal
                    title={editingBook ? "Редактировать книгу" : "Добавить книгу"}
                    open={isModalVisible}
                    onOk={() => form.submit()}
                    okText={editingBook ? "Изменить" : "Добавить"}
                    onCancel={() => setIsModalVisible(false)}
                    cancelText="Отменить"
                    confirmLoading={submitting}
                    width={800}
                >
                    <Form form={form} onFinish={handleSubmit} layout="vertical">
                        <Form.Item
                            name="name"
                            label="Название"
                            rules={[
                                { required: true, message: 'Введите название книги' },
                                { max: 255, message: 'Длина книги не должна превышать 255 символов' }
                            ]}
                        >
                            <Input placeholder="Введите название книги" />
                        </Form.Item>

                        <Form.Item
                            name="authorIds"
                            label="Авторы"
                        >
                            <Select
                                mode="multiple"
                                showSearch
                                optionFilterProp="children"
                                placeholder="Выберите авторов"
                                allowClear={false}
                            >
                                {Array.isArray(authors) && authors.map(author => (
                                    <Option key={author.id} value={author.id}>
                                        {author.name}
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>

                        <Form.Item
                            name="categoryIds"
                            label="Жанры"
                            initialValue={[]}
                        >
                            <Select
                                mode="multiple"
                                showSearch
                                optionFilterProp="children"
                                placeholder="Выберите жанры"
                            >
                                {Array.isArray(categories) && categories.map(category => (
                                    <Option key={category.id} value={category.id}>
                                        {category.name}
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name="year"
                            label="Год написания"
                        >
                            <Input placeholder="Введите год написания книги" />
                        </Form.Item>
                        <Form.Item
                            name="pageAmount"
                            label="Кол-во страниц"
                            rules={[
                                { required: true, message: 'Введите кол-во страниц книги' },
                            ]}
                        >
                            <InputNumber placeholder="Введите кол-во страниц книги" style={{ width: '100%' }} controls={false} min={1} />
                        </Form.Item>
                    </Form>
                </Modal>
            </div>
        </Spin>
    );
};

export default BookList;