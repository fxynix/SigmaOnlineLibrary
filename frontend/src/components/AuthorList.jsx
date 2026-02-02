import React, { useState, useEffect } from 'react';
import {Table, Button, Space, Modal, Form, Input, Select, message, Spin, Tag} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import axios from 'axios';

const { Column } = Table;
const { Option } = Select;

const AuthorList = () => {
    const [authors, setAuthors] = useState([]);
    const [books, setBooks] = useState([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingAuthor, setEditingAuthor] = useState(null);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchAuthors();
        fetchBooks();
    }, []);

    const fetchAuthors = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/authors`);
            setAuthors(response.data);
        } catch (error) {
            message.error('Не удалось загрузить авторов');
        } finally {
            setLoading(false);
        }
    };

    const fetchBooks = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/books`);
            setBooks(response.data);
        } catch (error) {
            message.error('Не удалось загрузить книги');
        } finally {
            setLoading(false);
        }
    };

    const showModal = (author = null) => {
        setEditingAuthor(author);
        form.resetFields();
        if (author) {
            form.setFieldsValue({
                name: author.name,
                info: author.info,
                bookIds: books
                    .filter(book => author.books?.includes(book.name))
                    .map(book => book.id) || []
            });
        } else {
            form.setFieldsValue({
                name: '',
                bookIds: []
            });
        }
        setIsModalVisible(true);
    };

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const requestData = {
                name: values.name,
                info: values.info,
                bookIds: values.bookIds};

            if (editingAuthor) {
                await axios.put(
                    `${process.env.REACT_APP_API_URL}/authors/${editingAuthor.id}`,
                    requestData
                );
                message.success('Автор отредактирован успешно');
            } else {
                await axios.post(
                    `${process.env.REACT_APP_API_URL}/authors`,
                    requestData
                );
                message.success('Автор добавлен успешно');
            }

            fetchAuthors();
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
                message.error(error.response?.data?.message || 'Не удалось сохранить автора');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        Modal.confirm({
            title: 'Удалить автора',
            content: 'Вы уверены, что хотите удалить этого автора?',
            okText: 'Удалить',
            okType: 'danger',
            cancelText: 'Отменить',
            onOk: async () => {
                try {
                    await axios.delete(`${process.env.REACT_APP_API_URL}/authors/${id}`);
                    message.success('Автор успешно удален');
                    fetchAuthors();
                } catch (error) {
                    message.error(error.response?.data?.message || 'Не удалось удалить автора');
                }
            }
        });
    };

    return (
        <Spin spinning={loading} tip="Loading...">
            <span className="name-text">Все авторы</span>
            <div className="container">
                <div className="actions">
                    <Button type="primary"
                            icon={<PlusOutlined/>}
                            onClick={() => showModal()}
                            className="add-button">
                        Добавить автора
                    </Button>
                </div>

                <Table dataSource={authors} rowKey="id">
                    <Column title="ФИО" dataIndex="name" key="name"/>
                    <Column title="Информация" dataIndex="info" key="info"
                            render={(text) => (text ? text : '-')}
                    />
                    <Column
                        title="Книги"
                        key="books"
                        render={(_, author) => (
                            Array.isArray(author.books) && author.books.length > 0 ? (
                                author.books.map((book, index) => (
                                    <Tag key={index}>
                                        {typeof book === 'string' ? book : book.name}
                                    </Tag>
                                ))
                            ) : '-'
                        )}
                    />
                    <Column
                        title="Действия"
                        key="action"
                        render={(_, author) => (
                            <Space size="middle">
                                <Button
                                    type="link"
                                    icon={<EditOutlined/>}
                                    onClick={() => showModal(author)}
                                />
                                <Button
                                    type="link"
                                    icon={<DeleteOutlined/>}
                                    onClick={() => handleDelete(author.id)}
                                    danger
                                />
                            </Space>
                        )}
                    />
                </Table>

                <Modal
                    title={editingAuthor ? "Редактировать автора" : "Добавить автора"}
                    open={isModalVisible}
                    onOk={() => form.submit()}
                    okText={editingAuthor ? "Изменить" : "Добавить"}
                    onCancel={() => setIsModalVisible(false)}
                    cancelText="Отменить"
                    confirmLoading={submitting}
                    width={700}
                >
                    <Form form={form} onFinish={handleSubmit} layout="vertical">
                        <Form.Item
                            name="name"
                            label="ФИО"
                            rules={[
                                { required: true, message: 'Введите имя автора' },
                                { max: 255, message: 'Имя автора не должно быть длинее 255 символов'},
                                { min: 2, message: 'Имя автора не должна быть короче 2 символов'}
                            ]}
                        >
                            <Input placeholder="Введите имя автора"/>
                        </Form.Item>
                        <Form.Item
                            name="info"
                            label="Информация"
                        >
                            <Input placeholder="Введите информацию про автора"/>
                        </Form.Item>
                        <Form.Item
                            name="bookIds"
                            label="Книги"
                        >
                            <Select
                                mode="multiple"
                                showSearch
                                optionFilterProp="children"
                                placeholder="Выберите книги"
                                allowClear
                            >
                                {books.map(book => (
                                    <Option key={book.id} value={book.id}>
                                        {book.name}
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>
                    </Form>
                </Modal>
            </div>
        </Spin>
    );
};

export default AuthorList;
