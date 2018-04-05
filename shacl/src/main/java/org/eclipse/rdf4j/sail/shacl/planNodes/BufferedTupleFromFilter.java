/*******************************************************************************
 * Copyright (c) 2016 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.sail.SailException;

import java.util.LinkedList;

/**
 * @author Håvard Ottestad
 */
public class BufferedTupleFromFilter implements PlanNode, PushBasedPlanNode, SupportsDepthProvider {


	private CloseableIteration<Tuple, SailException> parentIterator;

	LinkedList<Tuple> next = new LinkedList<>();
	private DepthProvider depthProvider;

	@Override
	public CloseableIteration<Tuple, SailException> iterator() {
		return new CloseableIteration<Tuple, SailException>() {

			private void calculateNext() {
				if (parentIterator != null) {
					while (next.isEmpty() && parentIterator.hasNext()) {
						parentIterator.next();
					}
				}
			}

			@Override
			public void close() throws SailException {
				if (parentIterator != null) {
					parentIterator.close();
				}
			}

			@Override
			public boolean hasNext() throws SailException {
				calculateNext();
				return !next.isEmpty();
			}

			@Override
			public Tuple next() throws SailException {
				calculateNext();

				return next.removeLast();
			}


			@Override
			public void remove() throws SailException {

			}
		};
	}

	@Override
	public int depth() {
		return depthProvider.depth() + 1;
	}

	@Override
	public void push(Tuple t) {
		if (t != null) {
			next.addFirst(t);
		}
	}

	@Override
	public void parentIterator(CloseableIteration<Tuple, SailException> iterator) {
		parentIterator = iterator;
	}


	@Override
	public void receiveDepthProvider(DepthProvider depthProvider) {
		this.depthProvider = depthProvider;
	}
}
