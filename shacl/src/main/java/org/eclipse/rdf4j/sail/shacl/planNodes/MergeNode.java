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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Håvard Ottestad
 */
public class MergeNode implements PlanNode {

	PlanNode[] nodes;


	public MergeNode(PlanNode... nodes) {
		this.nodes = nodes;
	}

	@Override
	public CloseableIteration<Tuple, SailException> iterator() {
		return new CloseableIteration<Tuple, SailException>() {

			List<CloseableIteration<Tuple, SailException>> iterators = Arrays.stream(nodes).map(PlanNode::iterator).collect(Collectors.toList());

			Tuple[] peekList = new Tuple[nodes.length];

			Tuple next;

			private void calculateNext() {

				if (next != null) {
					return;
				}

				for (int i = 0; i < peekList.length; i++) {
					if (peekList[i] == null) {
						CloseableIteration<Tuple, SailException> iterator = iterators.get(i);
						if (iterator.hasNext()) {
							peekList[i] = iterator.next();
						}
					}
				}

				Tuple sortedFirst = null;
				int sortedFirstIndex = -1;

				for (int i = 0; i < peekList.length; i++) {
					if (peekList[i] == null) {
						continue;
					}

					if (sortedFirst == null) {
						sortedFirst = peekList[i];
						sortedFirstIndex = i;
					} else {
						if (peekList[i].compareTo(sortedFirst) < 0) {
							sortedFirst = peekList[i];
							sortedFirstIndex = i;
						}
					}

				}

				if (sortedFirstIndex >= 0) {
					peekList[sortedFirstIndex] = null;
				}

				next = sortedFirst;
			}


			@Override
			public void close() throws SailException {
				iterators.forEach(CloseableIteration::close);
			}

			@Override
			public boolean hasNext() throws SailException {
				calculateNext();
				return next != null;
			}


			@Override
			public Tuple next() throws SailException {
				calculateNext();

				Tuple temp = next;
				next = null;
				return temp;
			}

			@Override
			public void remove() throws SailException {

			}
		};
	}

	@Override
	public int depth() {
		return Arrays.stream(nodes).mapToInt(PlanNode::depth).max().orElse(0) + 1;

	}
}
