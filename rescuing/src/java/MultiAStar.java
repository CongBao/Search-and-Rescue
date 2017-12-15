import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/**
 * An algorithm to solve traveling salesman problem using brute force searching.
 * A* algorithm is used to calculate the edges.
 *
 * @author Cong Bao
 */
public class MultiAStar {

	private AStar aStar;

	/**
	 * Constructor of {@link MultiAStar}.
	 *
	 * @param model
	 *            an instance of {@link GridWorldModel}
	 */
	public MultiAStar(GridWorldModel model) {
		aStar = new AStar(model);
	}

	/**
	 * Find an optimal path covering all targets.
	 *
	 * @param start
	 *            the start {@link Location}
	 * @param targets
	 *            target {@link Location}
	 * @return a list of {@link Location} contains the cells in the found path
	 *         (expect the start point)
	 */
	public List<Location> findPath(Location start, List<Location> targets) {
		Node root = new Node(start.x, start.y);
		List<Node> leaves = new LinkedList<>();
		Queue<Node> build = new LinkedList<>();
		build.offer(root);
		while (!build.isEmpty()) {
			Node next = build.poll();
			if (next.height == targets.size()) {
				while (!build.isEmpty()) {
					leaves.add(build.poll());
				}
				break;
			}
			next.children = new LinkedList<>();
			for (Location target : targets) {
				Node node = new Node(target.x, target.y);
				if (next.contains(node)) {
					continue;
				}
				node.height = next.height + 1;
				node.value = next.value + aStar.findPath(new Location(next.x, next.y), target).size();
				node.parent = next;
				next.children.add(node);
				build.offer(node);
			}
		}
		Collections.sort(leaves);
		Node shortest = leaves.get(0);
		List<Location> path = new LinkedList<>();
		while (shortest.parent != null) {
			path.addAll(aStar.findPath(shortest.toLocation(), shortest.parent.toLocation()));
			shortest = shortest.parent;
		}
		Collections.reverse(path);
		path.remove(0);
		path.add(leaves.get(0).toLocation());
		return path;
	}

	private class Node implements Comparable<Node> {

		private int x;
		private int y;

		private int height;

		private int value;

		private Node parent;
		private List<Node> children;

		public Node(int x, int y) {
			this(x, y, 0, 0, null, null);
		}

		public Node(int x, int y, int height, int value, Node parent, List<Node> children) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.value = value;
			this.parent = parent;
			this.children = children;
		}

		/**
		 * Whether the given node is existing in this node's ancestors.
		 *
		 * @param node
		 *            the node to query
		 * @return true if the node is in ancestors
		 */
		public boolean contains(Node node) {
			if (equals(node)) {
				return true;
			} else if (parent == null) {
				return false;
			} else {
				return parent.contains(node);
			}
		}

		/**
		 * Transfer to {@link Location}.
		 *
		 * @return an instance of {@link Location} represents the node
		 */
		public Location toLocation() {
			return new Location(x, y);
		}

		@Override
		public int compareTo(Node other) {
			return value - other.value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		private MultiAStar getOuterType() {
			return MultiAStar.this;
		}

	}

	/* test method
	public static void main(String[] args) {
		GridWorldModel model = new ArenaModel();
		Location start = new Location(2, 4);
		List<Location> targets = new LinkedList<>();
		targets.add(new Location(1, 1));
		targets.add(new Location(3, 3));
		targets.add(new Location(3, 5));
		targets.add(new Location(4, 4));
		targets.add(new Location(5, 1));
		List<Location> result = new MultiAStar(model).findPath(start, targets);
		System.out.println("Steps: " + result.size());
		for (Location cell : result) {
			System.out.println("[" + cell + "]");
		}
	}
	 */

}
