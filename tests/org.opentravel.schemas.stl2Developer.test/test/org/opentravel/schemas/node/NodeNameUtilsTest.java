/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.node;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodeNameUtilsTest {

	public static final String[] INVALID_SUFFIX = new String[] { "_Type", "_type" };
	private static final String ID_REFERENCE_SUFFIX = "Ref";

	@BeforeClass
	public static void beforeTests() {
		new ModelNode(new TLModel());
		new MainController(); // isolate from previous test (I think)
		// When run in all tests i got 4 type assignment errors where listener did not match target
	}

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	/**
	 * <pre>
	 * Element = InitialCap
	 * ---Type---
	 * SimpleList -> InitialCap + 's'
	 * DetailList -> InitialCap + 'Detail'
	 * UNASSIGNED -> 'Undefined' (check Node.UNDEFINED_PROPERTY_TEXT)
	 * </pre>
	 */

	@Test
	public void elementAssignedSimpleType() {
		String typeName = "lowerCase";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignVWA("VWA").setName(typeName)
				.build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = "LowerCase";
		assertEquals(expected, actual);
		assertEquals(expected, pn.getName()); // make sure user could reassign name
	}

	@Test
	public void elementAssignedComplexType() {
		String typeName = "lowerCase";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignBuisnessObject("BO")
				.setName(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = "BO";
		assertEquals(expected, actual);
		assertEquals(expected, pn.getName()); // make sure name comes from assigned BO
	}

	@Test
	public void elementWithLowerCase() {
		String typeName = "lowerCase";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignVWA("VWA").setName(typeName)
				.build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = "LowerCase";
		assertEquals(expected, actual);
		assertEquals(expected, pn.getName());
	}

	@Test
	public void elementRef() {
		// Create a BO to reference
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("BO");
		bo.getIDFacet().addProperty(PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build());

		// Make sure the assigned name uses the full name used by the compiler.
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE).build();
		pn.setAssignedType(bo);
		QName name = PropertyCodegenUtils.getDefaultSchemaElementName((NamedEntity) bo.getTLModelObject(), true);
		assert name.getLocalPart().equals(pn.getName());

		// Facets get named by the compiler to use their long name. Make sure GUI matches.
		pn.setAssignedType((TypeProvider) bo.getIDFacet());
		name = PropertyCodegenUtils.getDefaultSchemaElementName(
				(NamedEntity) ((Node) bo.getIDFacet()).getTLModelObject(), true);
		assert name.getLocalPart().equals(pn.getName());
	}

	@Test
	public void elementWithSimpleList() {
		String typeName = "CO";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).makeSimpleList(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = typeName + "s";
		assertEquals(expected, actual);
	}

	@Test
	public void elementWithSimpleListWithInitialName() {
		String typeName = "CO";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).setName("InitialName")
				.makeSimpleList(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = typeName + "s";
		assertEquals(expected, actual);
	}

	@Test
	public void elementWithDetailList() {
		String typeName = "CO";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).makeDetailList(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		// will append Detail suffix
		String expected = XsdCodegenUtils.getGlobalElementName(pn.getAssignedTLNamedEntity()).getLocalPart();
		assertEquals(expected, actual);
	}

	@Test
	public void elementWithUnassigedType() {
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = Node.UNDEFINED_PROPERTY_TXT;
		assertEquals(expected, actual);
	}

	/**
	 * Attribute = lowerCase
	 */

	@Test
	public void attributeWithUpperCase() {
		String actual = "InvalidAttribute";
		String expected = "invalidAttribute";
		assertEquals(expected, NodeNameUtils.fixAttributeName(actual));
	}

	/**
	 * <pre>
	 * Indicator = lowerCase + 'Ind'
	 * Can not start with 'is'
	 * </pre>
	 */

	@Test
	public void indicatorWithUpperCase() {
		String actual = "InvalidIndicatorElement";
		String expected = "invalidIndicatorElement" + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorName(actual));
	}

	@Test
	public void indicatorWithBannedPrefixShouldRemove() {
		String expected = "invalidIndicatorElementInd";
		String actual = NodeNameUtils.IndicatorBannedPrefix + expected;
		assertEquals(expected, NodeNameUtils.fixIndicatorName(actual));
	}

	/**
	 * <pre>
	 * IndicatorElement = upperCase + 'Ind'
	 * </pre>
	 * 
	 * Can not start with 'is'
	 */

	@Test
	public void indicatorElementShouldInitialCapAndSuffix() {
		String actual = "invalidIndicatorElement";
		String expected = "InvalidIndicatorElement" + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithInvalidSuffixShouldAdd() {
		String actual = "InvalidIndicatorElementind";
		String expected = actual + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithBannedPrefixShouldRemove() {
		String expected = "InvalidIndicatorElementInd";
		String actual = NodeNameUtils.IndicatorBannedPrefix + expected;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithCorrectShouldDoNothing() {
		String actual = "InvalidIndicatorElementInd";
		assertEquals(actual, NodeNameUtils.fixIndicatorElementName(actual));
	}

	/**
	 * <pre>
	 * Simple Type = upperCase 
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */

	@Test
	public void simpleTypeWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	}

	// @Test
	// public void simpleTypeWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixSimpleTypeName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	// @Test
	// public void simpleTypeWithValidSuffixAndInvalidSuffixes() {
	// String typeName = "LowerCase";
	// // String actual = typeName + "_typeType";
	// String expected = typeName;
	// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	// }

	// @Test
	// public void simpleTypeWithValidSuffixAndInvalidSuffixes2() {
	// String typeName = "LowerCase";
	// String expected = typeName;
	// // String actual = typeName + "type";
	// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	// }

	@Test
	public void simpleTypeWithInvalidSuffixInTheMiddle() {
		String expected = "LowerTypeCase";
		String actual = expected;
		assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	}

	private void checkNameAgainsInvalidSuffixes(final String initialName, NameFixer fix, String... suffixes) {
		for (String suffix : INVALID_SUFFIX) {
			final String beforeFix = initialName + suffix;
			final String actual = fix.fixName(beforeFix);
			addError(collector, new Runnable() {

				@Override
				public void run() {
					assertEquals("Before fix name: " + beforeFix, initialName, actual);

				}
			});
		}
	}

	private void addError(ErrorCollector collector, Runnable assertClause) {
		try {
			assertClause.run();
		} catch (Throwable e) {
			collector.addError(e);
		}
	}

	static interface NameFixer {

		String fixName(String name);
	}

	/**
	 * <pre>
	 * Enumeration = 'Enum_' + name
	 * </pre>
	 */
	@Test
	public void enumerationShouldStartWithPrefix() {
		// changed behavior 12/2015
		// String actual = "LowerCase";
		// String expected = NodeNameUtils.ENUM_PREFIX + actual;
		// assertEquals(expected, NodeNameUtils.fixEnumerationName(actual));
	}

	/**
	 * <pre>
	 * VWA = UpperCase
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */
	@Test
	public void vwaTypeWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		assertEquals(expected, NodeNameUtils.fixVWAName(actual));
	}

	// @Test
	// public void vwaTypeWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixVWAName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	/**
	 * <pre>
	 * CoreObject = UpperCase
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */
	@Test
	public void coreObjectWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		assertEquals(expected, NodeNameUtils.fixCoreObjectName(actual));
	}

	// @Test
	// public void coreObjectWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixCoreObjectName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	/**
	 * <pre>
	 * alias = UpperCase
	 * </pre>
	 */
	@Test
	public void aliasObjectWithLowerCase() {
		String actual = "lowerCase";
		String expected = "LowerCase";
		assertEquals(expected, NodeNameUtils.fixCoreObjectName(actual));
	}

	/**
	 * <pre>
	 * IDReference = UpperCase + 'Ref'
	 * </pre>
	 */
	// @Test
	// public void idReferenceTypeBO() {
	// String typeName = "Name";
	// String referencedType = "BOObject";
	// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE)
	// .assignBuisnessObject(referencedType).setName(typeName).build();
	//
	// String actual = NodeNameUtils.fixIdReferenceName(pn);
	// String expected = referencedType + ID_REFERENCE_SUFFIX;
	// assertEquals(expected, actual);
	// }

	@Test
	public void idReferenceTypeCoreObject() {
		String typeName = "Name";
		String referencedType = "CoreObjectObject";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE).assignCoreObject(referencedType)
				.setName(typeName).build();

		String actual = NodeNameUtils.fixIdReferenceName(pn);
		String expected = referencedType + ID_REFERENCE_SUFFIX;
		assertEquals(expected, actual);
	}

	@Test
	public void stripQueryFacetPrefix() {
		String boName = "BO";
		String facetName = "Query";
		BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addQueryFacet(facetName).get();
		FacetNode fn = (FacetNode) bo.getQueryFacets().get(0);
		Assert.assertEquals(getFaceName((TLFacet) fn.getTLModelObject()), fn.getName());

		String newName = NodeNameUtils.stripFacetPrefix(fn, null);

		Assert.assertEquals(facetName, newName);
	}

	@Test
	public void stripCustomFacetPrefix() {
		String boName = "BO";
		String facetName = "Custom";
		BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addCustomFacet(facetName).get();
		FacetNode fn = (FacetNode) bo.getCustomFacets().get(0);
		Assert.assertEquals(getFaceName((TLFacet) fn.getTLModelObject()), fn.getName());

		String newName = NodeNameUtils.stripFacetPrefix(fn, null);

		Assert.assertEquals(facetName, newName);
	}

	@Test
	public void stripCustomFacetPrefix2() {
		String boName = "BO";
		String facetName = "Custom_Custom";
		BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addCustomFacet(facetName).get();
		FacetNode fn = (FacetNode) bo.getCustomFacets().get(0);
		Assert.assertEquals(getFaceName((TLFacet) fn.getTLModelObject()), fn.getName());

		String newName = NodeNameUtils.stripFacetPrefix(fn, null);

		Assert.assertEquals(facetName, newName);
	}

	private String getFaceName(TLFacet tlFacet) {
		return XsdCodegenUtils.getGlobalTypeName(tlFacet);
	}
}
