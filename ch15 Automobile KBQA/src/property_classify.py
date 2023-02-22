from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
import pandas as pd
from sklearn.metrics import accuracy_score
import numpy as np

from utils import save_model, load_model, word_segment

def model_test(path):
    '''
    用于模型测试
    :param path: 存放训练数据的路径
    '''
    data = pd.read_csv(path, header=None)
    data.columns = ['text', 'label', 'word_seg']
    # 打乱顺序
    data = data.reindex(np.random.permutation(data.index))
    # 后6000条数据作为测试集
    train_data = data.iloc[:-6000, :]
    test_data = data.iloc[-6000:, :]
    x_train, y_train = train_data.word_seg, train_data.label
    x_test, y_test = test_data.word_seg, test_data.label
    # 利用TfidfVectorizer生成词频模型
    tf_idf = TfidfVectorizer(sublinear_tf=True, min_df=2, max_df=0.9, norm='l2', encoding='utf-8', ngram_range=(1, 4))
    tf_idf.fit(x_train)
    xtrain_tfidf = tf_idf.transform(x_train)
    xtest_tfidf = tf_idf.transform(x_test)
    print("训练集维度：", xtrain_tfidf.shape)
    print("测试集维度：", xtest_tfidf.shape)
    # 利用LinearSVC训练属性分类模型
    model = LinearSVC(class_weight='balanced', C=4.5, max_iter=1000)
    model.fit(xtrain_tfidf, y_train)
    y_pred = model.predict(xtest_tfidf)
    print('准确率为%s' % accuracy_score(y_test, y_pred))

def model_train(path):
    '''
    用于模型训练，并保存模型
    :param path:存放训练数据的路径
    '''
    data = pd.read_csv(path, header=None)
    data.columns = ['text', 'label', 'word_seg']
    data = data.reindex(np.random.permutation(data.index))
    train_data = data
    # 与model_test的区别是这里将所有的数据都加入训练集中，没有测试集
    x_train, y_train = train_data.word_seg, train_data.label
    tf_idf = TfidfVectorizer(sublinear_tf=True, min_df=1, max_df=0.9, norm='l2', encoding='utf-8', ngram_range=(1, 4))
    tf_idf.fit(x_train)
    xtrain_tfidf = tf_idf.transform(x_train)
    model = LinearSVC(class_weight='balanced', C=4.5, max_iter=1000)
    model.fit(xtrain_tfidf, y_train)
    wv_model_name = 'TF-IDF'
    model_path = "./model/" + wv_model_name + ".pkl"
    save_model(tf_idf, model_path)  # 保存tf-idf模型
    model_name = 'IR_LinearSVC'
    model_path = "./model/" + model_name + ".pkl"
    save_model(model, model_path)  # 保存分类模型

def model_pred(word_list):
    '''
    载入保存好的模型参数，进行预测
    :param word_list: 经过中文分词处理后的问句词语列表
    :return: 字符串：属性分类的结果
    '''
    # 加载训练中保存的模型，进行预测
    tf_idf = load_model(r'./model/TF-IDF.pkl')
    IR_model = load_model(r'./model/IR_LinearSVC.pkl')
    question = ' '.join(word_list)
    q = pd.DataFrame([{'text': question}]).text  # 问句需要转化为此类格式
    q_tfidf = tf_idf.transform(q)
    res = IR_model.predict(q_tfidf)
    return res[0]

def get_property(question_list, assist_words_dict, property_dict):
    '''
    读取辅助词和实体与属性概念树，进行属性推理得到属性结果
    :param question_list: 对问句进行中文分词处理后列表
    :param assist_words_dict: 辅助词字典数据
    :param property_dict: 属性与子属性字典
    :return: 存储属性分类与推理最终结果的字符串
    '''
    p = model_pred(question_list)  # 属性分类的结果
    res = p
    # 将属性中可能包含的辅助词进行替换
    for i in range(len(question_list)):
        if question_list[i] in assist_words_dict.keys():
            question_list[i] = assist_words_dict[question_list[i]]
    for pk, pv in property_dict.items():
        # 通过辅助词进行推理
        pk_list = word_segment(pk)
        for i in range(len(pk_list)):
            if pk_list[i] in assist_words_dict.keys():
                pk_list[i] = assist_words_dict[pk_list[i]]
        if p == pv:
            for w in question_list:
                if w in pk_list and w not in pv:
                    res = pk
    return res


if __name__ == '__main__':
    from filter import filter_text
    from data_loader import *
    from filepath import DataPath

    conception_tree_path = DataPath.conception_tree_path
    conception_tree = get_conception_tree(conception_tree_path)
    property_dict = get_property_dict(conception_tree)
    train_path = DataPath.property_classify_train_path
    # path = train_path
    # model_test(train_path)
    # model_train(train_path)
    # question = '你能回答我怎么增大后面空调的出风口的暖风风速吗'
    question = '这个空调怎么调高速度啊'
    # question = '调高速度'

    question_list = filter_text(word_segment(question))
    assist_words_dict = extract_assist_words_dict(DataPath.assist_words_path)

    property_result = get_property(question_list, assist_words_dict, property_dict)
    print(property_result)

