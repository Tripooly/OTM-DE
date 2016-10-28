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
/**
 * 
 */
package org.opentravel.schemas.testUtils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ImpliedNodeType;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * @author Dave Hollander
 * 
 */
public class LoadFiles {
	private String filePath1 = "Resources" + File.separator + "testFile1.otm";
	private String filePath2 = "Resources" + File.separator + "testFile2.otm";
	private String filePath3 = "Resources" + File.separator + "testFile3.otm";
	private String filePath4 = "Resources" + File.separator + "testFile4.otm";
	private String path5 = "Resources" + File.separator + "testFile5.otm";
	private String path5c = "Resources" + File.separator + "testFile5-Clean.otm";
	private String path6 = "Resources" + File.separator + "testFile6.otm";
	private String path7 = "Resources" + File.separator + "testFile7.otm";
	private String contextFile1 = "Resources" + File.separator + "base_library.otm";
	private String contextFile2 = "Resources" + File.separator + "facets1_library.otm";
	private String contextFile3 = "Resources" + File.separator + "facets2_library.otm";
	private String choiceFile1 = "Resources" + File.separator + "testFile_Choice1.otm";

	private MainController mc;
	private int nodeCount = 0;

	public LoadFiles() {
	}

	/**
	 * Assure Test files can be read.
	 * 
	 * @throws Exception
	 */
	@Test
	public void loadFiles() throws Exception {
		this.mc = new MainController();
		int libCnt = Node.getAllLibraries().size(); // built-ins

		// check special files
		ProjectController pc = mc.getProjectController();
		ProjectNode proj = pc.getDefaultProject();
		loadFile_Choice(proj);
		libCnt++;
		loadFile6(proj);
		libCnt++;
		// duplicate ns/name - loadFile7(proj);

		try {
			loadFile1(mc);
			libCnt++;
			loadFile2(mc);
			libCnt++;
			loadFile3(mc);
			libCnt++;
			loadFile4(mc);
			libCnt++;
			loadFile5(mc);
			libCnt++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(libCnt, Node.getAllLibraries().size());

		loadTestGroupA(mc);

	}

	@Test
	public void builtInTests() {
		MainController mc = new MainController();
		new LoadFiles();
		mc.getModelNode().visitAllNodes(new NodeTesters().new TestNode());
	}

	/**
	 * Load files 1 through 5 into default project. No tests.
	 */
	public int loadTestGroupA(MainController mc) throws Exception {
		ProjectController pc = mc.getProjectController();

		List<File> files = new ArrayList<File>();
		files.add(new File(filePath1));
		files.add(new File(filePath2));
		files.add(new File(filePath3));
		files.add(new File(filePath4));
		files.add(new File(path5));
		pc.getDefaultProject().add(files);
		int libCnt = pc.getDefaultProject().getChildren().size();
		return libCnt;
	}

	/**
	 * Remove any nodes with bad assignments. The removed nodes will not pass Node_Tests/visitNode().
	 */
	public void cleanModel() {
		NodeVisitor srcVisitor = new NodeTesters().new ValidateTLObject();
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			// LOGGER.debug("Cleaning Library " + ln + " with " +
			// ln.getDescendants_TypeUsers().size()
			// + " type users.");
			for (TypeUser n : ln.getDescendants_TypeUsers()) {
				if (n.getAssignedType() instanceof ImpliedNode) {
					if (((ImpliedNode) n.getAssignedType()).getImpliedType().equals(ImpliedNodeType.UnassignedType)) {
						// LOGGER.debug("Removing " + n + " due to unassigned type.");
						if (!((Node) n).isDeleted())
							((Node) n).getOwningComponent().delete();
						continue;
					}
				}
				if (n.getAssignedTLObject() == null) {
					// LOGGER.debug("Removing " + n + " due to null TL_TypeObject.");
					if (((Node) n).getOwningComponent() != null)
						((Node) n).getOwningComponent().delete();
					continue;
				}
				// if (!n.getTypeClass().verifyAssignment()) {
				// // LOGGER.debug("Removing " + n + " due to type node mismatch.");
				// n.getOwningComponent().delete();
				// continue;
				// }
				try {
					srcVisitor.visit((INode) n);
				} catch (IllegalStateException e) {
					// LOGGER.debug("Removing " + n + " due to: " + e.getLocalizedMessage());
					((Node) n).getOwningComponent().delete();
				}

			}
		}
	}

	/**
	 * Load a file into the default project. NOTE - if the file is already open an assertion error will be thrown. NOTE
	 * - the returned library might not be the one opened, it might be one imported.
	 * 
	 * @param main
	 *            controller
	 * @param file
	 *            path
	 * @return library node containing model created from the OTM file.
	 */
	public LibraryNode loadFile(MainController thisModel, String path) {
		ProjectNode project = thisModel.getProjectController().getDefaultProject();
		return loadFile(project, path);
	}

	public LibraryNode loadFile(ProjectNode project, String path) {
		assertTrue("Must have a non-null project.", project != null);
		List<File> files = new ArrayList<File>();
		files.add(new File(path));
		assertTrue("File must exist.", files.get(0).exists());
		project.add(files);

		// Then - project must have the new library
		assertTrue("Project must have children.", project.getChildren().size() > 0);
		URL u = URLUtils.toURL(new File(System.getProperty("user.dir") + File.separator + path));
		LibraryNode ln = null;
		for (LibraryNode lib : project.getLibraries()) {
			URL url = lib.getTLaLib().getLibraryUrl();
			if (u.equals(url)) {
				ln = lib;
				break;
			}

		}
		assertTrue("Library must be found that has the correct url.", ln != null);
		assertTrue("Library must have children.", ln.getChildren().size() > 0);
		return ln;
	}

	// Has 1 unassigned types.
	public LibraryNode loadFile1(MainController mc) {
		// ModelNode model = mc.getModelNode();
		LibraryNode ln = loadFile(mc, filePath1);
		Assert.assertNotNull(ln);
		Assert.assertTrue(ln.getChildren().size() > 2);
		// Assert.assertNotNull(model);
		// Assert.assertNotNull(model.getTLModel());
		Assert.assertTrue(Node.getAllLibraries().size() > 2);
		// Assert.assertTrue(Node.getNodeCount() > 100);
		// Assert.assertTrue(model.getUnassignedTypeCount() >= 0);
		return ln;
	}

	public LibraryNode loadFile2(ProjectNode project) {
		return loadFile(project, filePath2);
	}

	// Has 14 unassigned types - references to STL2 library
	public LibraryNode loadFile2(MainController thisModel) {
		// ModelNode model = thisModel.getModelNode();
		LibraryNode ln = loadFile(thisModel, filePath2);
		Assert.assertNotNull(ln);
		// Assert.assertNotNull(model);
		// Assert.assertNotNull(model.getTLModel());
		// Assert.assertTrue(Node.getNodeCount() > 100);
		// Assert.assertTrue("Bad count: " + model.getUnassignedTypeCount(), model.getUnassignedTypeCount() >= 14);
		return ln;
	}

	public LibraryNode loadFile3(MainController thisModel) {
		LibraryNode ln = loadFile(thisModel, filePath3);

		Assert.assertNotNull(ln);
		Assert.assertTrue(ln.getChildren().size() > 1);
		Assert.assertTrue(ln.getDescendants_NamedTypes().size() >= 3);

		return ln;
	}

	public LibraryNode loadFile4(MainController thisModel) {
		LibraryNode ln = loadFile(thisModel, filePath4);
		Assert.assertNotNull(ln);
		Assert.assertTrue(ln.getChildren().size() > 1);
		List<Node> d = ln.getDescendants_NamedTypes();
		Assert.assertEquals(7, d.size());
		return ln;
	}

	public LibraryNode loadFile5(MainController thisModel) {
		LibraryNode ln = loadFile(thisModel, path5);
		return ln;
	}

	public LibraryNode loadFile5Clean(MainController thisModel) {
		LibraryNode ln = loadFile(thisModel, path5c);
		return ln;
	}

	public LibraryNode loadFile6(MainController thisModel) {
		LibraryNode ln = loadFile(thisModel, path6);
		return ln;
	}

	public LibraryNode loadFile6(ProjectNode project) {
		return loadFile(project, path6);
	}

	/**
	 * No Errors with resource and choice objects.
	 */
	public LibraryNode loadFile7(ProjectNode project) {
		return loadFile(project, path7);
	}

	/**
	 * No Errors with contextual facets.
	 */
	public LibraryNode loadFile_FacetBase(ProjectNode project) {
		return loadFile(project, contextFile1);
	}

	/**
	 * No Errors with contextual facets.
	 */
	public LibraryNode loadFile_Choice(ProjectNode project) {
		return loadFile(project, choiceFile1);
	}

	/**
	 * No Errors with contextual facets.
	 */
	public LibraryNode loadFile_Facets1(ProjectNode project) {
		return loadFile(project, contextFile2);
	}

	/**
	 * No Errors with contextual facets.
	 */
	public LibraryNode loadFile_Facets2(ProjectNode project) {
		return loadFile(project, contextFile3);
	}

	/**
	 * Load the test files 1 though 5 and visit all nodes. Then either remove or delete each node.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSuiteTests() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		lf.loadFile1(mc);

		lf.loadFile5(mc);
		lf.loadFile3(mc);
		lf.loadFile4(mc);
		lf.loadFile2(mc);

		mc.getModelNode().visitAllNodes(new NodeTesters().new TestNode());

		for (INode n : new ArrayList<>(mc.getModelNode().getChildren())) {
			nodeCount++;
			actOnNode(n);
		}

	}

	private void actOnNode(INode n) {
		if (n instanceof TypeUser && n instanceof TypeProvider)
			((TypeUser) n).setAssignedType((TypeProvider) n);
		n.setName("TEST");
		switch (nodeCount % 3) {
		case 0:
			n.removeFromLibrary();
			n.close();
			break;
		case 1:
			n.delete();
			break;
		case 2:
			n.delete();
		}
	}

}
