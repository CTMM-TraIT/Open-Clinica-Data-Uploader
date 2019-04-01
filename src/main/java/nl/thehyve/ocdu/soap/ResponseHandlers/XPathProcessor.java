/*
 * Copyright © 2016-2019 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu.soap.ResponseHandlers;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes XPath queries while storing previously complied XPathExpressions for reuse.
 * Created by jacob on 3/25/19.
 */
public class XPathProcessor {


	public Map<String, XPathExpression> compiledXPathExpressions;


	public XPathProcessor() {
		compiledXPathExpressions = new HashMap<>();
	}


	public synchronized Object process(String expression, Object item, QName returnType) throws XPathExpressionException {
		XPathExpression xPathExpression = retrieveXPathExpression(expression);
		return xPathExpression.evaluate(item, returnType);
	}

	private XPathExpression retrieveXPathExpression(String expression) throws XPathExpressionException {
		if (compiledXPathExpressions.containsKey(expression)) {
			return compiledXPathExpressions.get(expression);
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xpath.compile(expression);
		compiledXPathExpressions.put(expression, xPathExpression);
		return xPathExpression;
	}
}
