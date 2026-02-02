import React, { useState, useEffect } from 'react';
import {Table, Button, Space, Modal, Form, Input, message, Spin} from 'antd';
import {EditOutlined, DeleteOutlined, UserOutlined, MailOutlined, LockOutlined} from '@ant-design/icons';
import axios from 'axios';
import Column from "antd/es/table/Column";

const UserList = ({ currentUser, onUserUpdate }) => {
    const [users, setUsers] = useState([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchUsers();
    }, []);

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

    const handleUpdate = async (values) => {
        try {
            const updateData = {
                name: values.name,
                email: values.email,
                password: values.password,
            };

                const response = await axios.put(
                    `${process.env.REACT_APP_API_URL}/users/${editingUser.id}`,
                    updateData
                );

                fetchUsers();

                if (currentUser && currentUser.id === editingUser.id) {
                    onUserUpdate(response.data);
                    message.success('Профиль пользователя изменен успешно');
                } else {
                    message.success('Пользователь отредактирован успешно');
                }

            setIsModalVisible(false);
        } catch (error) {
            if (error.response?.status === 400 && error.response.data) {
                const errorMessages = Object.entries(error.response.data)
                    .map(([field, message]) => `${message}`)
                    .join('\n');

                message.error({
                    content: (
                        <div style={{whiteSpace: 'pre-line'}}>
                            {errorMessages}
                        </div>
                    ),
                    duration: 5,
                });
            } else if (error.response?.status === 409) {
                message.error({
                    content: error.response.data,
                    duration:5,
                });
            } else {
                message.error('Не удалось сохранить пользователя');
            }
        }
    };

    const showModal = (user = null) => {
        setEditingUser(user);
        form.resetFields();
        if (user) {
            form.setFieldsValue({
                name: user.name,
                email: user.email,
                password: ''
            });
        }
        setIsModalVisible(true);
    };

    const handleDelete = async (id) => {
        Modal.confirm({
                title: 'Удалить пользователя',
                content: 'Вы уверены, что хотите удалить данного пользователя?',
                okText: 'Удалить',
                okType: 'danger',
                cancelText: 'Отменить',
                onOk: async () => {
                    try {
                        await axios.delete(`${process.env.REACT_APP_API_URL}/users/${id}`);
                        message.success('Пользователь удален успешно');
                        fetchUsers();
                    } catch (error) {
                        message.error('Не удалось удалить пользователя');
                    }
                }
            }
        )
    };

    return (
        <Spin spinning={loading} tip="Loading...">
            <span className="name-text">Все пользователи</span>
            <div className="container">

                <Table dataSource={users} rowKey="id">
                    <Column title="Имя пользователя" dataIndex="name" key="name"/>
                    <Column title="Email" dataIndex="email" key="email"/>
                    <Column
                        title="Действия"
                        key="action"
                        render={(_, user) => (
                            <Space size="middle">
                                <Button
                                    type="link"
                                    icon={<EditOutlined/>}
                                    onClick={() => showModal(user)}
                                />
                                <Button
                                    type="link"
                                    icon={<DeleteOutlined/>}
                                    onClick={() => handleDelete(user.id)}
                                    danger
                                />
                            </Space>
                        )}
                    />
                </Table>

                <Modal
                    title="Изменить пользователя"
                    open={isModalVisible}
                    onOk={() => form.submit()}
                    okText="Изменить"
                    onCancel={() => setIsModalVisible(false)}
                    cancelText="Отменить"
                >
                    <Form form={form} onFinish={handleUpdate} layout="vertical">
                        <Form.Item
                            name="name"
                            label="Имя пользователя"
                            rules={[
                                {required: true, message: 'Введите имя пользователя'},
                                {max: 100, message: 'Имя пользователя должен быть короче 101 символа'}
                            ]}

                        >
                            <Input prefix={<UserOutlined/>} placeholder="Введите имя пользователя"/>
                        </Form.Item>
                        <Form.Item
                            name="email"
                            label="Email"
                            rules={[{required: true, type: 'email', message: 'Введите корректный email'}]}
                        >
                            <Input prefix={<MailOutlined/>} placeholder="Введите email"/>
                        </Form.Item>
                        <Form.Item
                            name="password"
                            label="Пароль"
                            rules={[
                                {min: 8, message: 'Пароль должен быть не короче 8 символов'},
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined/>}
                                            placeholder="Чтобы оставить текущий пароль, оставьте поле пустым"/>
                        </Form.Item>
                    </Form>
                </Modal>
            </div>
        </Spin>
);
};

export default UserList;