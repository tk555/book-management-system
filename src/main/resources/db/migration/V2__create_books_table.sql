-- 書籍テーブル
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    title VARCHAR(500) NOT NULL,
    price INTEGER NOT NULL CHECK (price >= 0),
    publication_status VARCHAR(20) NOT NULL CHECK (publication_status IN ('UNPUBLISHED', 'PUBLISHED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- タイトルでの検索用インデックス
CREATE INDEX idx_books_title ON books(title);

-- 出版状況での絞り込み用インデックス
CREATE INDEX idx_books_publication_status ON books(publication_status);
