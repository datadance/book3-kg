import numpy as np
import json
from data_loader import load_data, rating_data
from train import recommend_train
from recommend import recommend_sys
from utils import Parameters

np.random.seed(555)
# json保存文件修正
class MyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        else:
            return super(MyEncoder, self).default(obj)

# In[参数设置]
args = Parameters.args

data = load_data(args)

# In[读取数据，执行推荐函数]
show_loss = False
show_topk = False
flag = 1
rating_np = rating_data(args)
result = recommend_train(args, data, rating_np, show_loss)
print('result generated')

# In[将result保存为json文件]
jsObj = json.dumps(result, cls=MyEncoder)
fileObject = open(args.dataset_path + '/result.json', 'w')
fileObject.write(jsObj)
fileObject.close()
print('result saved')

# In[推荐系统应用]
recommend_sys(args.dataset_path, args.num_recommend)
