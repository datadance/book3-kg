#!/usr/bin/env python
# coding=utf-8

try:
    import simplejson as json
except:
    import json
import sys
                    
import pymysql    
from pymysql import connections
from collections import defaultdict
 
class connec_mysql(object):
    def __init__(self):    
        self.conn = pymysql.connect(
            host='localhost',
            user='root',
            passwd='IlDM900900',
            db='kg_demo_movie',
            charset='utf8mb4',
            use_unicode=True
            )    
        self.cursor = self.conn.cursor()

    # def select_from_db(self, target_item, target_table, target_condition, target_value):
    #     self.cursor.execute("SELECT %s FROM %s WHERE %s = %s", (target_item, target_table, target_condition, target_value) )
    #     result = self.cursor.fetchall()
    #     return result
   # def movie_to_genre(self,operate_array,reference_array):  #把电影对应的类型做个转换（类型以及演员）

    def select_from_db(self, target_table, target_item):
        #self.cursor.execute("select movie_id from movie")
        self.cursor.execute("select %s from %s", (target_table, target_item) )
        result = self.cursor.fetchall()
        return result

    def get_json(self):
        # 读取电影演员的信息
        sql = "select * from person "
        self.cursor.execute(sql)  # 执行sql语句
        person_results = self.cursor.fetchall()  # 获取查询的所有记录

        #对person_to_movie进行查询
        sql = "select * from person_to_movie"
        self.cursor.execute(sql)
        person_to_movie_result = self.cursor.fetchall()

        # 构成movie-to-person字典
        person_to_movie_dict = {}
        for v_index in person_to_movie_result:
            a_key= v_index[1]
            b_value = v_index[0]
            count=0
            for persons in person_results:
                if(b_value==persons[0]):
                    person_to_movie_dict.setdefault(a_key, []).append(count)
                    count=0
                    break
                count+=1

        # 对movie_to_genre进行查询
        sql = "select * from movie_to_genre"
        self.cursor.execute(sql)
        movie_to_genre_result = self.cursor.fetchall()

        # 构成movie-to-genre字典
        movie_to_genre_dict = {}
        for v_index in movie_to_genre_result:
            a_key = v_index[0]
            b_value = v_index[1]
            movie_to_genre_dict.setdefault(a_key, []).append(b_value)

        # 对genre进行查询
        sql = "select * from genre"
        self.cursor.execute(sql)
        genre_result = self.cursor.fetchall()

        # 构成genre字典
        genre_dict = {}
        for v_index in genre_result:
            a_key = v_index[0]
            b_value = v_index[1]
            genre_dict.setdefault(a_key, []).append(b_value)

        # 以电影信息为主输出为Json文件
        # 01-以电影为主，把电影类型以及参演演员信息补充完整
        movie_info_list=[]
        sql = "select * from movie"
        self.cursor.execute(sql)
        movie_result= self.cursor.fetchall()
        for row in movie_result:
            movie_genre = []
            movie_actor = []
            # 添加电影类型#添加电影主演信息
            if row[0] in movie_to_genre_dict:
                genre_id = movie_to_genre_dict.get(row[0])
                for genre in genre_id:
                    movie_genre.append(genre_dict.get(genre))
            else:
                movie_genre.append('')
            row = row + (movie_genre, )

            if row[0] in person_to_movie_dict:
                actor_id = person_to_movie_dict.get(row[0])
                for actor in actor_id:
                #构造一下演员的dict信息
                    person_info={}
                    person_info['person_id']=person_results[actor][0]
                    person_info['person_birth_day'] = person_results[actor][1]
                    person_info['person_death_day'] = person_results[actor][2]
                    person_info['person_name'] = person_results[actor][3]
                    person_info['person_english_name'] = person_results[actor][4]
                    person_info['person_biography'] = person_results[actor][5]
                    person_info['person_birth_place'] = person_results[actor][6]
                    movie_actor.append(person_info)
            else:
                movie_actor.append('')
            row = row +(movie_actor,)
            #02-构造dict
            movie_info={}
            movie_info["movie_id"]=row[0]
            movie_info["movie_title"]=row[1]
            movie_info['movie_introduction']=row[2]
            movie_info['movie_rating']=row[3]
            movie_info['movie_release_date']=row[4]
            movie_info['movie_genre']=row[5]
            movie_info['movie_actor']=row[6]
        #03-转出Json
            movie_info_list.append(movie_info)
            with open("./external_data/record1.json", "a",encoding='utf-8') as f:
                f.write(json.dumps(movie_info,ensure_ascii=False))
                f.write("\n")
        f.close()
        print("加载入文件完成...")

if __name__ == "__main__":
    connect_sql = connec_mysql()
    connect_sql.get_json()
