-- 書籍と著者の中間テーブル（多対多の関係）
CREATE TABLE book_authors (
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id, author_id)
);

-- 著者から書籍を検索する用のインデックス（book_id, author_idの複合主キーで既にbook_id側はカバー）
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);
