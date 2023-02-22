import urllib.request
import urllib.parse
import json
from utils import word_segment, Trans_Source_Data

def entityCandidate(question: str, Trans_Source_Data):
	trans_entity = Trans_Source_Data.land_name_list + Trans_Source_Data.road_name_list + Trans_Source_Data.symptom2_list
	entityList = []
	urlPath = 'http://shuyantech.com/api/entitylinking/cutsegment?q='
	urlstr = urlPath + urllib.parse.quote(question)
	urlRST = urllib.request.urlopen(urlstr)
	dic = json.loads(urlRST.read().decode('utf-8'))
	for entity in dic['entities']:
		if len(entity) == 2:
			entityList.append(question[entity[0][0]:entity[0][1]])
	question_ws = word_segment(question)
	for w in question_ws:
		if w in trans_entity:
			entityList.append(w)
	entityList = list(set(entityList))
	if '' in entityList:
		entityList.remove('')
	return entityList

def answerCandidate(entityList: list, kb):
	answerList = []
	for entity in entityList:
		entity = '%' + entity + "%"
		answerList = answerList + kb.queryKB(entity)
	answerList = list(set(answerList))
	return answerList

if __name__ == '__main__':
	question = '铜陵路有什么问题'
	print(entityCandidate(question, Trans_Source_Data))
	from storeKB import KnowledgeBase
	kb = KnowledgeBase()
	print(answerCandidate(entityCandidate('铜陵路有什么问题', Trans_Source_Data), kb))
