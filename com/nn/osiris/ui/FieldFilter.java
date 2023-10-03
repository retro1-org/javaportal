/*
 * FieldFilter.java
 *
 * Started 2004
 *
 * Copyright Pearson Digital Learning
 */
package com.nn.osiris.ui;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Class that makes it easy to use the abstractdocument
 * to limit field sizes with reflection.
 */
class FieldFilter extends FieldFilterInterface
{
	public FieldFilter()
	{
	}

	public void setFilter(JTextField f,int size)
	{
	AbstractDocument doc = (AbstractDocument) f.getDocument();

		doc.setDocumentFilter(new SizeFilter(size));
	}
}