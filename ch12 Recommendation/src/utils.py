import argparse

class Parameters:
	parser = argparse.ArgumentParser()
	parser.add_argument('--n_epochs', type=int, default=20, help='the number of epochs')
	parser.add_argument('--dim', type=int, default=8, help='dimension of user and entity embeddings')
	parser.add_argument('--L', type=int, default=1, help='number of low layers')
	parser.add_argument('--H', type=int, default=1, help='number of high layers')
	parser.add_argument('--batch_size', type=int, default=4096, help='batch size')
	parser.add_argument('--l2_weight', type=float, default=1e-6, help='weight of l2 regularization')
	parser.add_argument('--lr_rs', type=float, default=0.02, help='learning rate of RS task')
	parser.add_argument('--lr_kge', type=float, default=0.01, help='learning rate of KGE task')
	parser.add_argument('--kge_interval', type=int, default=3, help='training interval of KGE task')
	parser.add_argument('--dataset_path', type=str, default='./data', help='which dataset path to use')
	parser.add_argument('--num_recommend', type=int, default=10, help='Number of recommended items ')
	args = parser.parse_args()
