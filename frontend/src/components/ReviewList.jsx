import React, { useState, useEffect } from 'react';
import {Table, Button, Space, Modal, Form, Input, message, Spin, InputNumber} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import axios from 'axios';
import {useParams} from "react-router-dom";

const { Column } = Table;

const ReviewList = () => {
    const { bookId } = useParams();
    const [reviews, setReviews] = useState([]);
    const [books, setBooks] = useState([]);
    const [users, setUsers] = useState([]);
    const [currentUser, setCurrentUser] = useState(null);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingReview, setEditingReview] = useState(null);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }

        if (bookId) {
            fetchReviews();
        }
        fetchBooks();
        fetchUsers();
    }, [bookId]);

    const fetchReviews = async () => {
        setLoading(true);
        try {
            const response = await axios
                .get(`${process.env.REACT_APP_API_URL}/books/${bookId}/reviews`);
            setReviews(response.data);
        } catch (error) {
            message.error('Не удалось загрузить отзывы');
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

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${process.env.REACT_APP_API_URL}/users`);
            setUsers(response.data);
        } catch (error) {
            message.error('Не удалось загрузить пользователей');
        } finally {
            setLoading(false);
        }
    };

    const showModal = (review = null) => {
        if (!currentUser) {
            message.warning('Для оставления отзыва необходимо авторизоваться');
            return;
        }

        setEditingReview(review);
        form.resetFields();
        if (review) {
            form.setFieldsValue({
                rating: review.rating,
                comment: review.comment || ''
            });
        } else {
            form.setFieldsValue({
                rating: null,
                comment: ''
            });
        }
        setIsModalVisible(true);
    };

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const requestData = {
                rating: values.rating,
                comment: values.comment,
                userId: currentUser?.id
            };

            if (editingReview) {
                await axios.put(
                    `${process.env.REACT_APP_API_URL}/books/${bookId}/reviews/${editingReview.id}`,
                    requestData
                );
                message.success('Отзыв отредактирован успешно');
            } else {
                await axios.post(
                    `${process.env.REACT_APP_API_URL}/books/${bookId}/reviews`,
                    requestData
                );
                message.success('Отзыв создан успешно');
            }

            fetchReviews();
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
                message.error(error.response?.data?.message || 'Не удалось сохранить отзыв');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        Modal.confirm({
            title: 'Удалить отзыв',
            content: 'Вы уверены, что хотите удалить этот отзыв?',
            okText: 'Удалить',
            okType: 'danger',
            cancelText: 'Отменить',
            onOk: async () => {
                try {
                    await axios.delete(`${process.env.REACT_APP_API_URL}/books/${bookId}/reviews/${id}`);
                    message.success('Отзыв удален успешно');
                    fetchReviews();
                } catch (error) {
                    message.error(error.response?.data?.message || 'Не удалось удалить отзыв');
                }
            }
        });
    };

    return (
        <Spin spinning={loading} tip="Loading...">
            <span className="name-text">Все отзывы</span>
            <div className="container">
                <div className="actions">
                    <Button type="primary"
                            icon={<PlusOutlined/>}
                            onClick={() => showModal()}
                            className="add-button">
                        Оставить отзыв
                    </Button>
                </div>

                <Table dataSource={reviews} rowKey="id">
                    <Column title="Пользователь" dataIndex="user" key="user"
                            render={(user) => (user?.name || '-')}
                    />
                    <Column title="Оценка" dataIndex="rating" key="rating" />
                    <Column title="Комментарий" dataIndex="comment" key="comment"
                            render={(text) => (text ? text : '-')}
                    />
                    <Column
                        title="Действия"
                        key="action"
                        render={(_, review) => (
                            <Space size="middle">
                                <Button
                                    type="link"
                                    icon={<EditOutlined/>}
                                    onClick={() => showModal(review)}
                                />
                                <Button
                                    type="link"
                                    icon={<DeleteOutlined/>}
                                    onClick={() => handleDelete(review.id)}
                                    danger
                                />
                            </Space>
                        )}
                    />
                </Table>

                <Modal
                    title={editingReview ? "Изменить отзыв" : "Добавить отзыв"}
                    open={isModalVisible}
                    onOk={() => form.submit()}
                    okText={editingReview ? "Изменить" : "Добавить"}
                    onCancel={() => setIsModalVisible(false)}
                    cancelText="Отменить"
                    confirmLoading={submitting}
                    width={700}
                >
                    <Form form={form} onFinish={handleSubmit} layout="vertical">
                        <Form.Item
                            name="rating"
                            label="Оценка"
                            rules={[
                                {required: true, message: 'Введите оценку!'},
                            ]}
                        >
                            <InputNumber placeholder="Введите оценку" style={{ width: '100%' }} min={1} max={5}/>
                        </Form.Item>
                        <Form.Item
                            name="comment"
                            label="Комментарий"
                        >
                            <Input placeholder="Введите комментарий"/>
                        </Form.Item>
                    </Form>
                </Modal>
            </div>
        </Spin>
    );
};

export default ReviewList;
