# -*- coding: utf-8 -*-
# __author__ = 'hugh'
import json

from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk


def set_data(inptfile):
    f = open(inptfile, 'r', encoding='UTF-8')
    print(f.readlines())


class ElasticObj:
    def __init__(self, index_name, index_type, ip):
        """
        :param index_name: 索引名称
        :param index_type: 索引类型
        """
        self.index_name = index_name
        self.index_type = index_type
        # 无用户名密码状态
        self.es = Elasticsearch([ip])
        # 用户名密码状态
        # self.es = Elasticsearch([ip],http_auth=('elastic', 'password'),port=9200)

    def create_index(self):
        '''
        创建索引,创建索引名称为ott，类型为ott_type的索引
        :param ex: Elasticsearch对象
        :return:
        '''
        # 创建映射
        _index_mappings = {
            "mappings": {
                self.index_type: {
                    "properties": {
                        "movie_actor": {
                            "properties": {
                                "person_biography": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_birth_day": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_birth_place": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_death_day": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_english_name": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_id": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                },
                                "person_name": {
                                    "type": "text",
                                    "copy_to": "full_info"
                                }
                            }
                        },
                        "movie_genre": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "movie_id": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "movie_introduction": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "movie_rating": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "movie_release_date": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "movie_title": {
                            "type": "text",
                            "copy_to": "full_info"
                        },
                        "full_info": {
                            "type": "text"
                        }
                    }
                }

            }
        }
        if self.es.indices.exists(index=self.index_name) is not True:
            res = self.es.indices.create(index=self.index_name, body=_index_mappings, ignore=400)
            print(res)

    # 插入数据
    def insert_data(self, inputfile):
        f = open(inputfile, 'r', encoding='UTF-8')
        data = []
        for line in f.readlines():
            # 把末尾的'\n'删掉
            line = line.replace("null", '-1')
            # print(line.strip())
            # 存入list
            data.append(line.strip())
        f.close()

        ACTIONS = []
        i = 1
        bulk_num = 2
        for list_line in data:
            # 去掉引号
            print(list_line)
            list_line = eval(list_line)
            # print(list_line)
            action = {
                "_index": self.index_name,
                "_type": self.index_type,
                "_id": i,  # _id 也可以默认生成，不赋值
                "_source": {
                    "movie_id": list_line["movie_id"],
                    "movie_title": list_line["movie_title"],
                    "movie_introduction": list_line["movie_introduction"],
                    "movie_rating": list_line["movie_rating"],
                    "movie_release_date": list_line["movie_release_date"],
                    "movie_genre": list_line["movie_genre"],
                    "movie_actor": list_line["movie_actor"]
                }
            }
            i += 1
            ACTIONS.append(action)
            # 批量处理
            if len(ACTIONS) == bulk_num:
                print('插入', i / bulk_num, '批数据')
                print(len(ACTIONS))
                success, _ = bulk(self.es, ACTIONS, index=self.index_name, raise_on_error=True)
                del ACTIONS[0:len(ACTIONS)]
                print(success)

        if len(ACTIONS) > 0:
            success, _ = bulk(self.es, ACTIONS, index=self.index_name, raise_on_error=True)
            del ACTIONS[0:len(ACTIONS)]
            print('Performed %d actions' % success)


if __name__ == '__main__':
    obj = ElasticObj("demo2", "en", ip="127.0.0.1")
    obj.create_index()
    obj.insert_data("record1.json")
