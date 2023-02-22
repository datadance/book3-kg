# -*- coding: utf-8 -*-
# @Time    : 2019/12/17 14:37
# @Author  : yjli
# @File    : run.py

from storeM2ID import Mention2ID
from storeKB import KnowledgeBase
from computeSimilar import Similar
from extractQA import entityCandidate,answerCandidate


if __name__ == '__main__':
    similar = Similar()
    m2id = Mention2ID()
    kb = KnowledgeBase()

    while True:
        questionstr = input('question:\n')
        if questionstr == 'end':
            break

        entityList = entityCandidate(questionstr,m2id)
        # print(entityList)

        if len(entityList)==0:
            print('no-answer')
            continue

        answerList = answerCandidate(entityList,kb)
        # print(answerList)
        #答案排名
        questionVec = similar.questionVec(questionstr,usePSEG=True)
        for idx in range(len(answerList)):
            answerVec = similar.answerVec('|||'.join(answerList[idx][:3]))
            answerList[idx] = answerList[idx] + (similar.vectorS(questionVec,answerVec),)
        answerList.sort(key=lambda element: element[3],reverse=True)
        for answer in answerList[:5]:
            print(answer)
    m2id.close()
    kb.close()

