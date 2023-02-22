import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.log4j.BasicConfigurator;
import java.io.*;
import java.util.*;

/**
 * ֪ʶ�����Է���թͼ�׵�����
 * @author zycheng4
 */
public class Inference {
	/**
	 * jena.rdf.model.Model�������֪ʶͼ��ģ��
	 * @see Model
	 * @see ModelFactory
	 */
	private static Model model = ModelFactory.createDefaultModel();
	/**
	 * @see DataGenerator
	 */
	private static DataGenerator generator;
	/**
	 * @see GenerateStoreData
	 */
	private static GenerateStoreData rdfGenerator;
	/**
	 * RDF�е�ǰ׺
	 */
	private static final String prefix = "http://jena.something.com/";

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();
		// ��������
		generator = new DataGenerator("data.json", 0.1, 18, 65,
				30000);
		generator.generate(120);
		// �洢����
		rdfGenerator = new GenerateStoreData("data.json");
		rdfGenerator.generateStoreRDF("database");
		Dataset dataset = TDBFactory.createDataset("database");

		// ��ȡ����
		dataset.begin(ReadWrite.READ);
		try {
			Model modelLoader = dataset.getDefaultModel();
			StmtIterator iterator = modelLoader.listStatements(null, null, (Resource)null);
			while (iterator.hasNext())
				model.add(iterator.nextStatement());
		} finally {
			dataset.end();
		}

		// ����
		System.out.println("========== [BATCH INFERENCE] ==========");
		batchInference();
		System.out.println("========== [NEW ENTRY INFERENCE] ==========");
		for (int i = 0; i < 20; i++) {
			newEntryInference();
			System.out.println(String.join("", Collections.nCopies(60, "-")));
		}
	}

	/**
	 * ����һ�����û�����������
	 */
	private static void newEntryInference() {
		List<String[]> result = generator.generateNewEntry(true);
		String[] newEntry = result.get(0);
		String[] newEntryGuard = result.get(1);
		model.add(rdfGenerator.generateSingleRDF(newEntry[0], Arrays.copyOfRange(newEntry, 1, newEntry.length)));
		if (!model.contains(model.createResource(newEntryGuard[0]), model.createProperty(prefix + "Name")))
			model.add(rdfGenerator.generateSingleRDF(newEntryGuard[0],
					Arrays.copyOfRange(newEntry, 1, newEntry.length)));
		Resource newId = model.getResource(newEntry[0]);

		Helper.printInfo(newEntry[0], Arrays.copyOfRange(newEntry, 1, newEntry.length), true, true);
		// Same Phone Number or Bank Account
		System.out.println("Same Phone Number: [New ID | Existing ID | Entry]");
		MyInference.hasSameTraverse(newId, model, "HasPhoneNumber", "PhoneNumberOwner");
		System.out.println("Same Bank Account: [New ID | Existing ID | Entry]");
		MyInference.hasSameTraverse(newId, model, "HasBankAccount", "BankAccountOwner");

		// iterative rule
		MyInference.traverseCheck(newId, model);
	}

	/**
	 * �����������еķ���թ֪ʶͼ�ף���������
	 * @throws FileNotFoundException �׳������ļ���ȡ�쳣
	 */
	private static void batchInference() throws FileNotFoundException {
		System.out.println("========== Rule Inference ==========");
		ruleInference();
		System.out.println("========== SPARQL Inference ==========");
		SPARQLInference();

		System.out.println("========== Traverse Check ==========");
		StmtIterator i = model.listStatements(null, model.getProperty(prefix + "Name"), (Resource) null);
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			MyInference.traverseCheck(s.getSubject(), model);
		}
	}

	/**
	 * �Զ�������������ִ������������
	 * @throws FileNotFoundException �׳������ļ���ȡ�쳣
	 */
	private static void ruleInference() throws FileNotFoundException {
		// ����
		BufferedReader br = new BufferedReader(new FileReader("inferenceRule.txt"));
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(Rule.rulesParserFromReader(br)));
		InfModel infMy = ModelFactory.createInfModel(reasoner, model);
		infMy.rebind();
		System.out.print("MyOwnRule: ");
		ValidityReport validity = infMy.validate();
		if (validity.isValid()) {
			System.out.println("OK");
		} else {
			System.out.println("Conflicts");
			for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); )
				System.out.printf(" - %s%n", i.next());
		}

		// ���������
		ArrayList<Property> conflicts = new ArrayList<>();
		String[] propLst = {"hasSameBankAccount", "hasSamePhoneNumber", "SelfGuarantor", "GuarantorLowerTrust"};
		for (String p : propLst) {conflicts.add(model.createProperty(prefix + p));}
		for (Property p : conflicts) {
			StmtIterator i = infMy.listStatements(null, p, (Resource) null);
			int count = 0;
			while (i.hasNext()) {
				Statement statement = i.nextStatement();
				System.out.printf(" - %s%n", PrintUtil.print(statement));
				count++;
			}
			System.out.printf("%s: %d%n", p.getLocalName(), count);
		}
	}

	/**
	 * ʹ��SPARQL�������������������
	 * ע�⣺����SPARQL���������޷��������ע�빥������������ʹ�ã�
	 */
	private static void SPARQLInference() {
//		System.out.println("-".repeat(50)); // Java 11
		System.out.printf("Has Same Bank Account: %d%n", MyInference.hasSame("HasBankAccount", model));

		System.out.println(String.join("", Collections.nCopies(80, "-")));
		System.out.printf("Has Same Phone Number: %d%n", MyInference.hasSame("HasPhoneNumber", model));

		System.out.println(String.join("", Collections.nCopies(80, "-")));
		System.out.printf("Guarantor Has Lower Trust Level: %d%n", MyInference.guarantorLowerTrust(model, true));

		System.out.println(String.join("", Collections.nCopies(80, "-")));
		System.out.printf("Guarantor Cannot Afford Loan: %d%n", MyInference.loanAmountCheck(model));
	}
}
