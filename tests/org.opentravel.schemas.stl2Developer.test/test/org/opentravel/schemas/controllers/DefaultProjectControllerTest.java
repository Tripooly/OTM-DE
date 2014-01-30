
package org.opentravel.schemas.controllers;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * @author Pawel Jedruch
 * 
 */
public class DefaultProjectControllerTest extends BaseProjectTest {

    @Test
    public void closeShouldRemoveProject() throws LibrarySaveException {
        ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
        pc.close(toCloseProject);
        Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
    }

    @Test
    public void closeAllShouldRemoveProject() throws LibrarySaveException {
        ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
        pc.closeAll();
        Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
    }

    @Test
    public void closeAllShouldRemoveDefaultProject() throws LibrarySaveException {
        pc.closeAll();
        Assert.assertFalse(Node.getModelNode().getChildren().contains(defaultProject));
    }

    @Test
    public void closeShouldReloadDefaultProject() throws LibrarySaveException {
        LibraryNode lib = LibraryNodeBuilder.create("TestLib",
                pc.getDefaultProject().getNamespace(), "a", Version.emptyVersion).build(
                pc.getDefaultProject(), pc);
        ProjectNode defaultProjectBeforeClose = pc.getDefaultProject();

        Assert.assertEquals(1, defaultProjectBeforeClose.getLibraries().size());
        pc.close(pc.getDefaultProject());

        ProjectNode defaultProjectAfterClose = pc.getDefaultProject();
        Assert.assertNotSame(defaultProjectBeforeClose, defaultProjectAfterClose);
        Assert.assertEquals(1, defaultProjectAfterClose.getLibraries().size());
        LibraryNode libAfterClose = defaultProjectAfterClose.getLibraries().get(0);
        Assert.assertNotSame(lib, libAfterClose);
        Assert.assertEquals(lib.getName(), libAfterClose.getName());
    }

    @Test
    public void crossLibraryLinks() throws LibrarySaveException {
        LibraryNode local1 = LibraryNodeBuilder.create("LocalOne",
                defaultProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0)).build(
                defaultProject, pc);
        SimpleTypeNode so = ComponentNodeBuilder.createSimpleObject("SO")
                .assignType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE)).get();
        local1.addMember(so);

        LibraryNode local2 = LibraryNodeBuilder.create("LocalTwo",
                defaultProject.getNamespace() + "/Test/Two", "o2", new Version(1, 0, 0)).build(
                defaultProject, pc);
        PropertyNode property = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
                .setName("Reference").assign(so).build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").addToSummaryFacet(property)
                .get();
        local2.addMember(co);
        mc.getLibraryController().saveAllLibraries(false);
        Set<String> expectedImports = new HashSet<String>();
        for (TLNamespaceImport imported : local2.getTLLibrary().getNamespaceImports()) {
            expectedImports.add(imported.getNamespace());
        }

        mc.getLibraryController().remove(defaultProject.getLibraries());

        File local2File = URLUtils.toFile(local2.getTLLibrary().getLibraryUrl());
        defaultProject.add(Collections.singletonList(local2File));
        LibraryNode reopenedLibrary = defaultProject.getLibraries().get(0);
        TLLibrary tlLib = reopenedLibrary.getTLLibrary();

        Set<String> actaulsImports = new HashSet<String>();
        for (TLNamespaceImport imported : tlLib.getNamespaceImports()) {
            actaulsImports.add(imported.getNamespace());
        }

        for (String e : expectedImports) {
            if (!actaulsImports.contains(e)) {
                fail("Missing imported namespace: " + e);
            }
        }
    }

}
