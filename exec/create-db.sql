create table todos (
	id bigint primary key auto_increment,
    todo character varying(172),
    finished boolean default false
);