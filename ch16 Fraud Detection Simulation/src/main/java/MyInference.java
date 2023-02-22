import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

/**
 * SPARQL与本体模型的推理方法
 * @author zycheng4
 */
public class MyInference {
	/**
	 * RDF中的前缀
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
	 * 使用SPARQL验证用户是否共享银行账户或电话号码
	 * @param term 银行账户或电话号码的属性，分别为HasBankAccount与HasPhoneNumber
	 * @param model 知识图谱模型
	 * @return 存在信息不一致的个体数量
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
	 * 使用SPARQL验证用户是否存在担保人的信用评级低于借贷人的情况
	 * @param model 知识图谱模型
	 * @param CN 选择是否输出中文
	 * @return 担保人的信用评级低于借贷人的个体数量
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
		if (CN) trustLevels = new String[] {"超高信用", "高信用", "中信用", "低信用", "无信用", "黑名单"};
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
	 * 使用SPARQL验证用户是否存在担保人的额度低于借贷人的贷款数额的情况
	 * @param model 知识图谱模型
	 * @return 担保人的额度低于借贷人的贷款数额的个体数量
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
	 * 验证新用户是否与他人共享银行账户或电话号码
	 * @param newId 新用户id
	 * @param model 知识图谱模型
	 * @param toData 用户节点至银行账户或电话号码节点的属性，分别为HasBankAccount与HasPhoneNumber
	 * @param fromData 银行账户或电话号码节点至用户节点的属性，分别为BankAccountOwner与PhoneNumberOwner
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
	 * 验证新用户是否存在担保人的额度低于借贷人的贷款数额的情况，以及新用户的担保链上是否存在黑名单用户
	 * @param newId 新用户id
	 * @param model 知识图谱模型
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
