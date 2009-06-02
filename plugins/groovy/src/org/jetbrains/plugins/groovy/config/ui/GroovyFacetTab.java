/*
 * Copyright 2000-2008 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.config.ui;

import com.intellij.facet.impl.ui.FacetContextChangeListener;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Condition;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.config.GrailsLibraryManager;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.config.GroovyFacetConfiguration;
import org.jetbrains.plugins.groovy.config.LibraryManager;
import org.jetbrains.plugins.groovy.config.GroovyLibraryManager;
import org.jetbrains.plugins.groovy.config.AbstractGroovyLibraryManager;

import javax.swing.*;

/**
 * @author ilyas
 */
public class GroovyFacetTab extends FacetEditorTab {

  public static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.config.ui.GroovyFacetTab");

  private JPanel myPanel;
  private JRadioButton myCompile;
  private JRadioButton myCopyToOutput;
  private JCheckBox myIsGrails;
  private JPanel myManagedLibrariesPanel;

  private final ManagedLibrariesEditor myManagedLibrariesEditor;
  private final GroovyFacetConfiguration myConfiguration;

  public GroovyFacetTab(final FacetEditorContext editorContext, GroovyFacetConfiguration configuration, FacetValidatorsManager validatorsManager) {
    myConfiguration = configuration;
    myManagedLibrariesEditor = new ManagedLibrariesEditor(editorContext, validatorsManager, AbstractGroovyLibraryManager.EP_NAME);

    myManagedLibrariesPanel.add(myManagedLibrariesEditor.getComponent());

    myManagedLibrariesEditor.getFacetEditorContext().addFacetContextChangeListener(new FacetContextChangeListener() {
      public void moduleRootsChanged(ModifiableRootModel rootModel) {
        final boolean hasGrails = hasGrailsLibrary();
        if (!myIsGrails.isEnabled() && hasGrails) {
          myIsGrails.setEnabled(true);
          myIsGrails.setSelected(true);
        } else if (!hasGrails) {
          myIsGrails.setEnabled(false);
          myIsGrails.setSelected(false);
        }
      }

      public void facetModelChanged(@NotNull Module module) {
      }
    });
    myManagedLibrariesEditor.shouldHaveLibrary(new Condition<LibraryManager>() {
      public boolean value(LibraryManager libraryManager) {
        return libraryManager instanceof GrailsLibraryManager || libraryManager instanceof GroovyLibraryManager;
      }
    }, "Groovy/Grails is not configured yet");
  }

  private boolean hasGrailsLibrary() {
    return myManagedLibrariesEditor.findUsedLibrary(GrailsLibraryManager.class) != null;
  }

  @Nls
  public String getDisplayName() {
    return GroovyBundle.message("groovy.sdk.configuration");
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    if (myCompile.isSelected() != myConfiguration.isCompileGroovyFiles()) {
      return true;
    }
    if (isGrailsApplication() != myConfiguration.getGrailsApplication()) {
      return true;
    }

    return false;
  }

  @Override
  public String getHelpTopic() {
    return super.getHelpTopic();
  }

  public void apply() throws ConfigurationException {
    myConfiguration.setCompileGroovyFiles(myCompile.isSelected());
    myConfiguration.setGrailsApplication(isGrailsApplication());
  }

  @Nullable
  private Boolean isGrailsApplication() {
    return myIsGrails.isEnabled() ? myIsGrails.isSelected() : null;
  }

  public void reset() {
    (myConfiguration.isCompileGroovyFiles() ? myCompile : myCopyToOutput).setSelected(true);
    final Boolean isGrails = myConfiguration.getGrailsApplication();
    myIsGrails.setEnabled(isGrails != null);
    myIsGrails.setSelected(isGrails != null && isGrails.booleanValue());
    myManagedLibrariesEditor.updateLibraryList();
  }

  public void disposeUIResources() {
  }

}
