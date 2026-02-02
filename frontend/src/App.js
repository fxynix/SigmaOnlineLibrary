import '@ant-design/v5-patch-for-react-19';
import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Dropdown, Modal, Form, Input, Button, message } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  InfoCircleOutlined,
  MailOutlined,
  LockOutlined
} from '@ant-design/icons';
import axios from 'axios';
import BookList from "./components/BookList";
import AuthorList from "./components/AuthorList";
import CategoryList from "./components/CategoryList";
import UserList from "./components/UserList";
import UserProfilePage from "./components/UserProfilePage";
import logoImage from './assets/logo.png';
import ReviewList from "./components/ReviewList";

const { Header, Content } = Layout;

const AppContent = () => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [isAuthModalVisible, setIsAuthModalVisible] = useState(false);
  const [authForm] = Form.useForm();
  const [registration, setRegistration] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleAuth = async () => {
    try {
      const values = await authForm.validateFields();

      if(registration) {
        const response = await axios.post(`${process.env.REACT_APP_API_URL}/users`, values);
        const userData = {
          id: response.data.userId,
          name: response.data.name,
          email: response.data.email,
          token: response.data.token
        };

        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
      }
      else
      {
        const response = await axios.post(`${process.env.REACT_APP_API_URL}/users/login`, values);
        const userData = {
          id: response.data.userId,
          name: response.data.name,
          email: response.data.email,
          token: response.data.token
        };

        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
      }

      setIsAuthModalVisible(false);
      message.success('Авторизация пройдена успешно');
    } catch (error) {
      message.error(error.response?.data || 'Авторизация не прошла успешно');
    }
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    message.success('Logged out successfully');

    if (location.pathname.includes('/users/') ||
        new URLSearchParams(location.search).has('authorId')) {
      navigate('/books');
    }
  };

  const handleUserUpdate = (updatedUser) => {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
  };

  return (
      <Layout style={{ minHeight: '100vh', backgroundColor: '#ffffff'}}>
        <Header className="header">
          <div style={{display: 'flex', alignItems: 'center'}}>
            <div className="header-left">
              <Link to="/" style={{display: 'flex', alignItems: 'center', textDecoration: 'none'}}>
                <img src={logoImage} alt="Logo" style={{height: 37}}/>
                <span className="header-logo-text">Книги</span>
              </Link>
            </div>

            <div className="nav-links">
              <Link to="/books" className="nav-link">Главное</Link>
              <Link to="/authors" className="nav-link">Авторы</Link>
              <Link to="/categories" className="nav-link">Жанры</Link>
              {/* Измененная кнопка "Пользователи" */}
              <span
                  className="nav-link"
                  style={{ cursor: 'pointer' }}
                  onClick={() => {
                    Modal.warning({
                      title: 'В разработке',
                      content: 'Раздел пользователей находится в разработке',
                      okText: 'Понятно'
                    });
                  }}
              >
                  Пользователи
                </span>
            </div>
          </div>

          <div className="header-right">
            {user ? (
                <UserDropdown user={user} onLogout={handleLogout}/>
            ) : (
                <>
                  {/* Измененная кнопка "Зарегистрироваться" */}
                  <Button onClick={() => {
                    Modal.warning({
                      title: 'В разработке',
                      content: 'Регистрация находится в разработке',
                      okText: 'Понятно'
                    });
                  }} className="auth-button">
                    Зарегистрироваться
                  </Button>
                  {/* Измененная кнопка "Войти" */}
                  <Button onClick={() => {
                    Modal.warning({
                      title: 'В разработке',
                      content: 'Авторизация находится в разработке',
                      okText: 'Понятно'
                    });
                  }} className="auth-button">
                    Войти
                  </Button>
                </>
            )}
          </div>
        </Header>
        <Content>
          <div className="site-layout">
            <Routes>
              <Route path="/books" element={<BookList />} />
              <Route path="/authors" element={<AuthorList />} />
              <Route path="/categories" element={<CategoryList />} />
              <Route path="/books/:bookId/reviews" element={<ReviewList />} />
              <Route
                  path="/users"
                  element={<UserList currentUser={user} onUserUpdate={handleUserUpdate} />}
              />
              <Route
                  path="/users/:id/profile"
                  element={<UserProfilePage user={user} onUserUpdate={handleUserUpdate} />}
              />
              <Route path="/" element={<BookList />} />
            </Routes>
          </div>
        </Content>

        {/* Удаляем или комментируем модальное окно авторизации, так как оно больше не нужно */}
        {/* <Modal
            title={registration ? "Регистрация" : "Авторизация"}
            open={isAuthModalVisible}
            onOk={handleAuth}
            onCancel={() => setIsAuthModalVisible(false)}
        >
          <Form form={authForm} layout="vertical">
            {(registration &&
            <Form.Item
                name="name"
                label="Имя"
                rules={[
                  { required: true, message: 'Введите свое имя' },
                ]}
            >
              <Input placeholder="Введите свое имя"/>
            </Form.Item>
            )}
            <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: 'Введите свой email' },
                  { type: 'email', message: 'Введите свой настоящий email' }
                ]}
            >
              <Input prefix={<MailOutlined />} placeholder="Введите свой email" />
            </Form.Item>
            <Form.Item
                name="password"
                label="Пароль"
                rules={[
                    { required: true, message: 'Введите пароль' },
                    { min: 8, message: 'Пароль должен быть длиной минимум 8 символов' },
                ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Введите свой пароль"/>
            </Form.Item>
          </Form>
        </Modal> */}
      </Layout>
  );
};

const UserDropdown = ({ user, onLogout }) => {
  const navigate = useNavigate();
  return (
      <Dropdown
          menu={{
            items: [
              {
                key: 'profile',
                icon: <InfoCircleOutlined />,
                label: 'Профиль',
                onClick: () => {
                  Modal.warning({
                    title: 'В разработке',
                    content: 'Раздел профиля находится в разработке',
                    okText: 'Понятно'
                  });
                }
              },
              {
                key: 'logout',
                icon: <LogoutOutlined />,
                label: 'Выйти',
                onClick: onLogout
              }
            ]
          }}
          trigger={['click']}
      >
        <span style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', marginRight: 24 }}>
          <span style={{ marginRight: 8 }}>{user.name}</span>
          <UserOutlined style={{ fontSize: '24px' }} />
        </span>
      </Dropdown>
  );
};

const App = () => {
  return (
      <Router>
        <AppContent />
      </Router>
  );
};

export default App;