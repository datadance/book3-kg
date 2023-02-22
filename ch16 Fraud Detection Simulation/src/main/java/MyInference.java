import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

/**
 * SPARQL�뱾��ģ�͵�������
 * @author zycheng4
 */
public class MyInference {
	/**
	 * RDF�е�ǰ׺
	 */
	private static final String prefix = "http://jena.something.com/";

/*
print method1 - UTF-8 only
		ResultSetFormatter.out(System.out, results, query);
print method2
		System.out.println(ResultSetFormatter.asText(results));
print method3
		List<QuerySolution> list = ResultSetFormatter.toList(results);
		for (QuerySolution qs : list)
			System.out.println(qs);
*/

	/**
	 * ʹ��SPARQL��֤�û��Ƿ��������˻���绰����
	 * @param term �����˻���绰��������ԣ��ֱ�ΪHasBankAccount��HasPhoneNumber
	 * @param model ֪ʶͼ��ģ��
	 * @return ������Ϣ��һ�µĸ�������
	 */
	static int hasSame(String term, Model model) {
		String queryString  = "PREFIX : <" + prefix + "> " +
				"SELECT ?id1 ?name1 ?id2 ?name2 WHERE {" +
					"?id1 :" + term + " ?account . " +
					"?id2 :" + term + " ?account . " +
					"?id1 :Name ?name1 . " +
					"?id2 :Name ?name2 . " +
					"FILTER (?id1 != ?id2)" +
				"}";
		Query query  = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		int numCols = results.getResultVars().size();
		int count = 0;
		System.out.println("ID1 \t\t\t\tName \tID2 \t\t\t\tName");
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			ArrayList<Long> idLst = new ArrayList<>();
			for (int col = 0; col < numCols; col+=2)
				idLst.add(Long.parseLong(qs.get(results.getResultVars().get(col)).toString()));
			if (idLst.get(0) < idLst.get(1)) {
				count++;
				ArrayList<String> nameLst = new ArrayList<>();
				for (int col = 1; col < numCols; col+=2)
					nameLst.add(qs.get(results.getResultVars().get(col)).toString());
				for (int col = 0; col < numCols; col++)
					System.out.printf("%s\t", (col % 2 == 0 ? idLst : nameLst).get(col / 2));
				System.out.println("");
			}
		}
		qe.close();
		return count;
	}

	/**
	 * ʹ��SPARQL��֤�û��Ƿ���ڵ����˵������������ڽ���˵����
	 * @param model ֪ʶͼ��ģ��
	 * @param CN ѡ���Ƿ��������
	 * @return �����˵������������ڽ���˵ĸ�������
	 */
	static int guarantorLowerTrust(Model model, boolean CN) {
		String queryString  = "PREFIX : <" + prefix + "> " +
				"SELECT ?user ?nameUser ?trustLevelUser ?guarantor ?nameGuarantor ?trustLevelGuarantor WHERE {" +
					"?user :GuaranteedBy ?guarantor . " +
					"?user :Trust ?trustLevelUser . " +
					"?guarantor :Trust ?trustLevelGuarantor . " +
					"?user :Name ?nameUser . " +
					"?guarantor :Name ?nameGuarantor . " +
					"FILTER (?trustLevelUser < ?trustLevelGuarantor)" +
				"}";
		Query query  = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		String[] trustLevels = {"Excellent", "High", "Medium", "Low", "Critical", "BlackList"};
		if (CN) trustLevels = new String[] {"��������", "������", "������", "������", "������", "������"};
		int numCols = results.getResultVars().size();
		int count = 0;
		System.out.println("UserID \t\t\t\tName \tTrust \t\tGuarantorID \t\tName \tTrust");
		while (results.hasNext()) {
			count++;
			QuerySolution qs = results.next();
			for (int col = 0; col < numCols; col++) {
				if (col % 3 == 2) {
					int trust = Integer.parseInt(qs.get(results.getResultVars().get(col)).toString().substring(0,1));
					System.out.printf("%s\t\t", trustLevels[trust]);
					continue;
				}
				System.out.printf("%s\t", qs.get(results.getResultVars().get(col)).toString());
			}
			System.out.println("");
		}
		qe.close();
		return count;
	}

	/**
	 * ʹ��SPARQL��֤�û��Ƿ���ڵ����˵Ķ�ȵ��ڽ���˵Ĵ�����������
	 * @param model ֪ʶͼ��ģ��
	 * @return �����˵Ķ�ȵ��ڽ���˵Ĵ�������ĸ�������
	 */
	static int loanAmountCheck(Model model) {
		String queryString  = "PREFIX : <" + prefix + "> " +
				"SELECT ?id1 ?name1 ?amount1 ?id2 ?name2 ?amount2 WHERE {" +
				"?id1 :GuaranteedBy ?id2 . " +
				"?id2 :Status ?id2Status . " +
				"?id1 :Amount ?amount1 . " +
				"?id2 :Amount ?amount2 . " +
				"?id1 :Name ?name1 . " +
				"?id2 :Name ?name2 . " +
				"FILTER (?amount1 > ?amount2)" +
				"}";
		Query query  = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		int numCols = results.getResultVars().size();
		int count = 0;
		System.out.println("UserID \t\t\t\tName \tAmount \t\tGuarantorID \t\tName \tAmount");
		while (results.hasNext()) {
			count++;
			QuerySolution qs = results.next();
			for (int col = 0; col < numCols; col++) {
				if (col % 3 == 2) {
					int amount = qs.get(results.getResultVars().get(col)).asLiteral().getInt();
					System.out.printf("%d\t\t", amount);
					continue;
				}
				System.out.printf("%s\t", qs.get(results.getResultVars().get(col)).toString());
			}
			System.out.println("");
		}
		qe.close();
		return count;
	}

	/**
	 * ��֤���û��Ƿ������˹��������˻���绰����
	 * @param newId ���û�id
	 * @param model ֪ʶͼ��ģ��
	 * @param toData �û��ڵ��������˻���绰����ڵ�����ԣ��ֱ�ΪHasBankAccount��HasPhoneNumber
	 * @param fromData �����˻���绰����ڵ����û��ڵ�����ԣ��ֱ�ΪBankAccountOwner��PhoneNumberOwner
	 */
	static void hasSameTraverse(@NotNull Resource newId, @NotNull Model model, String toData, String fromData) {
		Resource r = newId.getPropertyResourceValue(model.getProperty(prefix + toData));
		StmtIterator lst = r.listProperties(model.getProperty(prefix + fromData));
		while (lst.hasNext()) {
			Resource s = (Resource) lst.nextStatement().getObject();
			if (!s.getNameSpace().equals(newId.getNameSpace()))
				System.out.printf("%s | %s | %s%n", newId.getNameSpace(), s.getNameSpace(), r.getNameSpace());
		}
	}

	/**
	 * ��֤���û��Ƿ���ڵ����˵Ķ�ȵ��ڽ���˵Ĵ��������������Լ����û��ĵ��������Ƿ���ں������û�
	 * @param newId ���û�id
	 * @param model ֪ʶͼ��ģ��
	 */
	static void traverseCheck(@NotNull Resource newId, @NotNull Model model) {
		ArrayList<String> path = new ArrayList<>();
		path.add(newId.getNameSpace());
		Resource currID = newId;
		Property guaranteedBy = model.getProperty(prefix + "GuaranteedBy");
		Property loanAmount = model.getProperty(prefix + "Amount");
		int amount = 0;
		boolean containBlackListUser = false;
		while (model.contains(currID, guaranteedBy) &&
				!currID.getNameSpace().equals(currID.getPropertyResourceValue(guaranteedBy).getNameSpace())) {
			if (currID.getProperty(model.getProperty(prefix + "Status")).getObject().asLiteral().getInt() == 2)
				amount += currID.getProperty(loanAmount).getObject().asLiteral().getInt();
			currID = currID.getPropertyResourceValue(guaranteedBy);
			if (currID.getProperty(model.getProperty(prefix + "Trust")).getObject().asLiteral().getInt() == 5)
				containBlackListUser = true;
			path.add(currID.getNameSpace());
		}
		System.out.printf("Path: %s", String.join(" -> ", path));
		if (containBlackListUser) System.out.print(" | A Black List user is in the path.");
		if (amount > currID.getProperty(loanAmount).getObject().asLiteral().getInt())
			System.out.print(" | Loan amount requested is more than limit.");
		System.out.println("");
	}
}
