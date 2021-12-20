package org.klojang.template;

interface Part {

  /** The start index of this part within the template. */
  int start();

  Template getParentTemplate();
}
