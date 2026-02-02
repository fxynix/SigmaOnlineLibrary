import React, { useState, useEffect } from 'react';
import { Form, Input, Button, message } from 'antd';
import {UserOutlined, MailOutlined, LockOutlined, EditOutlined} from '@ant-design/icons';
import axios from "axios";

const UserProfilePage = ({ user, onUserUpdate }) => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (user) {
            form.setFieldsValue({
                name: user.name,
                email: user.email,
                password: ''
            });
        }
    }, [user, form]);

    const onFinish = async (values) => {
        try {
            setLoading(true);
            const response = await axios.put(`${process.env.REACT_APP_API_URL}/users/${user.id}`,
                {
                    name: values.name,
                    email: values.email,
                    password: values.password || undefined
                }
            );
            onUserUpdate(response.data);
            message.success('Профиль пользователя изменен успешно');
        } catch (error) {
            message.error('Не удалось отреактировать профиль');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 600, margin: '0 auto' }}>
            <h2>Профиль пользователя</h2>
            <Form
                form={form}
                onFinish={onFinish}
                layout="vertical"
            >
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

                <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading} icon={<EditOutlined />}>
                        Изменить профиль
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
};

export default UserProfilePage;