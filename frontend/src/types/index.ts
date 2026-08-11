export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
}

export interface User {
  id: number;
  username: string;
  nickname?: string;
  realName?: string;
  phone?: string;
  gender: number;
  age?: number;
  height?: number;
  weight?: number;
  allergyHistory?: string;
  medicalHistory?: string;
  role: number;
  avatar?: string;
  status: number;
  createTime?: string;
  token?: string;
}

export interface LoginDTO {
  username: string;
  password: string;
}

export interface RegisterDTO {
  username: string;
  password: string;
  nickname?: string;
  phone?: string;
  gender?: number;
  age?: number;
}

export interface LoginVO {
  token: string;
  user: User;
}

export interface ConsultationRequest {
  sessionId?: string;
  symptomDescription: string;
  symptomDuration?: string;
  age?: number;
  gender?: string;
  medicalHistory?: string;
  allergyHistory?: string;
}

export interface ConsultationVO {
  id: number;
  userId: number;
  nickname?: string;
  sessionId?: number;
  sessionKey?: string;
  symptomDescription: string;
  symptomDuration?: string;
  aiAdvice?: string;
  structuredAdvice?: string;
  riskLevel?: string;
  possibleDiseases?: string;
  suggestedDepartment?: string;
  status: number;
  failReason?: string;
  createTime?: string;
}

export interface HotQuestionVO {
  symptom: string;
  count: number;
}

export interface HealthKnowledge {
  id: number;
  title: string;
  category?: string;
  content: string;
  source?: string;
  status: number;
  createTime?: string;
  updateTime?: string;
}

export interface KnowledgeDTO {
  title: string;
  category?: string;
  content: string;
  source?: string;
}

export interface KnowledgeSearchDTO {
  query: string;
  topK?: number;
}

export interface KnowledgeSearchItem {
  content: string;
  score: number;
  metadata: Record<string, string>;
}
