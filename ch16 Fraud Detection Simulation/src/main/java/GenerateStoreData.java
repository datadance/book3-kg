import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import java.io.*;
import java.util.*;

/**
 * 知识抽取：将JSON数据转化为RDF三元组并存志Jena TDB中
 * @author zycheng4
 */
public class GenerateStoreData {
	/**
	 * 储存所有读取的用户信息：id，[相关信息]
	 */
	private HashMap<String, String[]> entityInfo;
	/**
	 * RDF中的前缀
	 */
	private static final String prefix = "http://jena.something.com/";
	/**
	 * 储存所有的属性
	 * @see Property
	 */
	private Property[] propLst;

	/**
	 * 设置属性并从JSON文件中读取数据。
	 * @param filePath JSON文件地址
	 * @throws FileNotFoundException JSON文件打开异常或读取异常
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
	 * 将数据转化为RDF三元组的形式存储到Jena TDB中
	 * @param location Jena TDB的存放位置
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
	 * 将一个用户的信息条目转化为对应的RDF三元组
	 * @param key 用户id
	 * @param data 用户的信息
	 * @return Jena.rdf.model.Model类对象，用于大批量传输三元组
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
