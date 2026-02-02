import React, { useState, useEffect } from 'react';
import {Table, Button, Space, Modal, Form, Input, message, Spin} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import axios from 'axios';

const { Column } = Table;

const CategoryList = () => {
    const [categories, setCategories] = useState([]);
    const [books, setBooks] = useState([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchCategories();
        fetchBooks();
    }, []);

    const fetchCategories = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/categories`);
            setCategories(response.data);
        } catch (error) {
            message.error('Не удалось загрузить жанры');
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

    const showModal = (category = null) => {
        setEditingCategory(category);
        form.resetFields();
        if (category) {
            form.setFieldsValue({
                name: category.name,
                bookIds: books
                    .filter(book => category.books?.includes(book.name))
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
                ...(editingCategory && { bookIds: values.bookIds || [] })
            };

            if (editingCategory) {
                await axios.put(
                    `${process.env.REACT_APP_API_URL}/categories/${editingCategory.id}`,
                    requestData
                );
                message.success('Жанр отредактирован успешно');
            } else {
                await axios.post(
                    `${process.env.REACT_APP_API_URL}/categories`,
                    { name: values.name }
                );
                message.success('Жанр добавлен успешно');
            }

            fetchCategories();
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
                message.error(error.response?.data?.message || 'Не удалось сохранить жанр');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        Modal.confirm({
            title: 'Удалить жанр',
            content: 'Вы уверены, что хотите удалить этот жанр?',
            okText: 'Удалить',
            okType: 'danger',
            cancelText: 'Отменить',
            onOk: async () => {
                try {
                    await axios.delete(`${process.env.REACT_APP_API_URL}/categories/${id}`);
                    message.success('Жанр успешно удален');
                    fetchCategories();
                } catch (error) {
                    message.error(error.response?.data?.message || 'Не удалось удалить жанр');
                }
            }
        });
    };

    return (
        <Spin spinning={loading} tip="Loading...">
            <span className="name-text">Все жанры</span>
            <div className="container">
                <div className="actions">
                    <Button type="primary"
                            icon={<PlusOutlined/>}
                            onClick={() => showModal()}
                            className="add-button">
                        Добавить жанр
                    </Button>
                </div>

                <Table dataSource={categories} rowKey="id">
                    <Column title="Название" dataIndex="name" key="name"/>
                    <Column title="Кол-во книг" dataIndex="count" key="count"/>
                    <Column
                        title="Действия"
                        key="action"
                        render={(_, category) => (
                            <Space size="middle">
                                <Button
                                    type="link"
                                    icon={<EditOutlined/>}
                                    onClick={() => showModal(category)}
                                />
                                <Button
                                    type="link"
                                    icon={<DeleteOutlined/>}
                                    onClick={() => handleDelete(category.id)}
                                    danger
                                />
                            </Space>
                        )}
                    />
                </Table>

                <Modal
                    title={editingCategory ? "Редактировать жанр" : "Добавить жанр"}
                    open={isModalVisible}
                    onOk={() => form.submit()}
                    okText={editingCategory ? "Изменить" : "Добавить"}
                    onCancel={() => setIsModalVisible(false)}
                    cancelText="Отменить"
                    confirmLoading={submitting}
                    width={700}
                >
                    <Form form={form} onFinish={handleSubmit} layout="vertical">
                        <Form.Item
                            name="name"
                            label="Название"
                            rules={[
                                { required: true, message: 'Введите название жанра' },
                                { max: 100, message: 'Название жанра не должно превышать 100 символов'}
                            ]}
                        >
                            <Input placeholder="Введите название жанра"/>
                        </Form.Item>
                    </Form>
                </Modal>
            </div>
        </Spin>
    );
};

export default CategoryList;