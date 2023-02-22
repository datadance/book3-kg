import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import java.io.*;
import java.util.*;

/**
 * ֪ʶ��ȡ����JSON����ת��ΪRDF��Ԫ�鲢��־Jena TDB��
 * @author zycheng4
 */
public class GenerateStoreData {
	/**
	 * �������ж�ȡ���û���Ϣ��id��[�����Ϣ]
	 */
	private HashMap<String, String[]> entityInfo;
	/**
	 * RDF�е�ǰ׺
	 */
	private static final String prefix = "http://jena.something.com/";
	/**
	 * �������е�����
	 * @see Property
	 */
	private Property[] propLst;

	/**
	 * �������Բ���JSON�ļ��ж�ȡ���ݡ�
	 * @param filePath JSON�ļ���ַ
	 * @throws FileNotFoundException JSON�ļ����쳣���ȡ�쳣
	 */
	public GenerateStoreData(String filePath) throws FileNotFoundException {
		Model model = ModelFactory.createDefaultModel();
		propLst = new Property[] {model.createProperty(prefix + "Name"),
				model.createProperty(prefix + "Age"), model.createProperty(prefix + "Gender"),
				model.createProperty(prefix + "Income"), model.createProperty(prefix + "Amount"),
				model.createProperty(prefix + "Trust"), model.createProperty(prefix + "Status")};

		Gson gson = new Gson();
		entityInfo = gson.fromJson(gson.newJsonReader(new FileReader(filePath)),
				new TypeToken<HashMap<String, String[]>>(){}.getType());
	}

	/**
	 * ������ת��ΪRDF��Ԫ�����ʽ�洢��Jena TDB��
	 * @param location Jena TDB�Ĵ��λ��
	 */
	public void generateStoreRDF(String location) {
		Dataset dataset = TDBFactory.createDataset(location);
		dataset.begin(ReadWrite.WRITE);
		try {
			Model modelStore = dataset.getDefaultModel();
			for (HashMap.Entry<String, String[]> entry: entityInfo.entrySet())
				modelStore.add(generateSingleRDF(entry.getKey(), entry.getValue()));
			dataset.commit();
		} finally {
			dataset.end();
		}
	}

	/**
	 * ��һ���û�����Ϣ��Ŀת��Ϊ��Ӧ��RDF��Ԫ��
	 * @param key �û�id
	 * @param data �û�����Ϣ
	 * @return Jena.rdf.model.Model��������ڴ�����������Ԫ��
	 */
	public Model generateSingleRDF(String key, String[] data) {
		Model model = ModelFactory.createDefaultModel();
		Resource id = model.createResource(key);
		for (int i = 0; i < propLst.length; i++)
			if ((i > 0 && i < 5 && data[i] != null) || (i > 4 && data[i + 2] != null))
				id.addLiteral(propLst[i], Integer.parseInt(i < 5 ? data[i] : data[i + 2]));
			else if (i == 0) id.addProperty(propLst[i], data[i]);

		Resource phoneNum = model.createResource(data[5]);
		Resource bankAcc = model.createResource(data[6]);
		model.add(id, model.createProperty(prefix + "HasPhoneNumber"), phoneNum);
		model.add(phoneNum, model.createProperty(prefix + "PhoneNumberOwner"), id);
		model.add(id, model.createProperty(prefix + "HasBankAccount"), bankAcc);
		model.add(bankAcc, model.createProperty(prefix + "BankAccountOwner"), id);

		String guarantor = data[data.length-2];
		if (guarantor != null)
			model.add(id, model.createProperty(prefix + "GuaranteedBy"), model.createResource(guarantor));
		return model;
	}
}
