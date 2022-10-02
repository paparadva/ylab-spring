create table ulab_edu.book
(
    id         bigint      not null,
    user_id  bigint      not null,
    title      varchar(50) not null,
    author     varchar(50) not null,
    page_count bigint      not null,
    constraint pk_ulab_edu_book_id primary key (id)
);

comment on table ulab_edu.book is 'Справочник используется для хранения книг';
comment on column ulab_edu.book.id is 'Идентификатор книги';
comment on column ulab_edu.book.user_id is 'Идентификатор пользователя';
comment on column ulab_edu.book.title is 'Заголовок';
comment on column ulab_edu.book.author is 'Автор';
comment on column ulab_edu.book.page_count is 'Количество страниц';

