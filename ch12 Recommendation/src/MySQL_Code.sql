# 创建数据库’kg_recommed’
create database kg_recommend;
show databases;
use kg_recommend;
# 创建表’book_data’
create table book_data(
    entity_id int not null,
    book_id int not null,
    language varchar(10) null,
    rating varchar(10) null,
    author varchar(10) null,
    country varchar(10) null,
    publisher varchar(10) null,
    style varchar(10) null,
    genre varchar(10) null,
    primary key ( entity_id )
    );
# 查看数据库导入数据路径，将需要导入的csv文件放到指定文件夹中
show variables like '%secure%';
load data infile 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\book_data.csv' into table book_data fields terminated by ','  lines terminated by '\r\n';

# 生成的图书知识图谱三元组存入MySQL数据库
create table kg_data(
    ind int not null,
    id int not null,
    relation varchar(20) not null,
    entity varchar(10) not null,
    primary key ( ind )
    );
load data infile 'C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Uploads\\kg.csv' into table kg_data 
fields terminated by ','  lines terminated by '\r\n';
