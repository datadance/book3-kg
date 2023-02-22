from storeKB import KnowledgeBase
from computeSimilar import Similar
from extractQA import entityCandidate,answerCandidate
from utils import Trans_Source_Data
import pandas as pd


def main():
    # 调用相似度计算模块
    similar = Similar()
    # 加载交通领域知识库
    kb = KnowledgeBase()
    while True:
        questionstr = input('question:\n')
        if questionstr == 'end':
            break
        # 获取实体识别的实体列表
        entityList = entityCandidate(questionstr, Trans_Source_Data)
        if len(entityList)==0:
            print('no-answer')
            continue
        # 获取答案三元组列表
        answerList = answerCandidate(entityList, kb)
        # 答案排名
        # 问句的向量建模
        questionVec = similar.questionVec(questionstr,usePSEG=False)
        for idx in range(len(answerList)):
            # 候选答案三元组的向量建模
            answerVec = similar.answerVec('|||'.join(answerList[idx][:3]), usePSEG=False)
            # 将问句向量与候选答案三元组向量进行相似度计算
            if not pd.isnull(similar.vectorS(questionVec,answerVec)):
                answerList[idx] = answerList[idx] + (similar.vectorS(questionVec,answerVec),)
            else: # 如果计算结果为空值，则赋值为0
                answerList[idx] = answerList[idx] + (0.0,)
        # 答案按分值排名
        answerList.sort(key=lambda element: element[3],reverse=True)
        # 打印答案排名前三位
        for answer in answerList[:3]:
            print(answer)

if __name__ == '__main__':
    main()
