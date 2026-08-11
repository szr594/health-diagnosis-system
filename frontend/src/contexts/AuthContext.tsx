import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import type { User, LoginDTO, RegisterDTO } from '../types';
import { userApi } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (data: LoginDTO) => Promise<void>;
  register: (data: RegisterDTO) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (savedToken && savedUser) {
      setToken(savedToken);
      try { setUser(JSON.parse(savedUser)); } catch {}
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (data: LoginDTO) => {
    const res = await userApi.login(data);
    const { token: t, user: u } = res.data.data;
    localStorage.setItem('token', t);
    localStorage.setItem('user', JSON.stringify(u));
    setToken(t);
    setUser(u);
  }, []);

  const register = useCallback(async (data: RegisterDTO) => {
    await userApi.register(data);
    const loginRes = await userApi.login({
      username: data.username,
      password: data.password,
    });
    const { token: t, user: u } = loginRes.data.data;
    localStorage.setItem('token', t);
    localStorage.setItem('user', JSON.stringify(u));
    setToken(t);
    setUser(u);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const res = await userApi.getInfo();
      const u = res.data.data;
      localStorage.setItem('user', JSON.stringify(u));
      setUser(u);
    } catch {}
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
