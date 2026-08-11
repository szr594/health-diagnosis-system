from typing import List, Optional

from pydantic import BaseModel, Field


class KnowledgeUploadRequest(BaseModel):

    id: int = Field(..., description="MySQL 中知识文档的主键 ID")
    title: str = Field(..., min_length=1, max_length=200, description="文档标题")
    category: str = Field(default="", max_length=50, description="分类")
    content: str = Field(..., min_length=1, description="知识内容原文")


class KnowledgeDeleteRequest(BaseModel):

    id: int = Field(..., description="MySQL 中知识文档的主键 ID")


class KnowledgeSearchRequest(BaseModel):

    query: str = Field(..., min_length=1, description="检索问题")
    top_k: int = Field(4, ge=1, le=10, description="返回条数")


class KnowledgeItem(BaseModel):

    doc_id: int = Field(..., description="文档 ID")
    title: str = Field(..., description="文档标题")
    category: str = Field("", description="分类")
    content: str = Field(..., description="命中片段")
    score: float = Field(0.0, description="相似度得分")
