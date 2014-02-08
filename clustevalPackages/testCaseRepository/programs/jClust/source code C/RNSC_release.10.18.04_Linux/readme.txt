/******************************************************************************
*******************************************************************************
*******************************************************************************
// All files written by Andrew D. King, 2004, except some of the linked list
// classes, but I honestly can't remember where I got them.  Feel free to
// distribute this in its unaltered form.  The author takes no responsibility
// for loss of sanity resulting from poorly written or documented code.
//
// Compiled under Fedora Core 2 Linux using the GNU C++ compiler verision 2.96.
// Probably won't work with newer or older versions or under different
// operating systems.
//
// As of September, 2004, the author will be reachable at the Department of
// Computer Science, McGill University, Montreal, Quebec, Canada.  The email
// address andrew.king@utoronto.ca will probably be in service until at least
// mid-2005.  Feel free to send any comments.
*******************************************************************************
*******************************************************************************
******************************************************************************/

NAME

	rnsc - Restricted Neighbourhood Search Cluster Algorithm


SYNOPSIS

	rnsc -g graph_filename
	     [-s] [-cnum] [-tnum] [-Tnum] [-nnum]
	     [-Nnum] [-enum] [-Dnum] [-dnum]
	     [-i input_clustering_filename]
	     [-o output_clustering_filename]

 
DESCRIPTION

	The RNSC algorithm takes a simple graph as input and clusters it,
	writing the final clustering to output_clustering_filename.  There are
	several user-input parameters that affect the algorithm's behaviour.


OPTIONS

     -g graph_filename
	RNSC reads the input graph from a filename, given as graph_filename.
	The default filename is "graph".  This file must have a very strict
	format.  The format is an adjacency list in which each edge appears
	only once.  The vertices are labelled with the integers 0, 1, ..., n-1.
	The list of neighbours for vertex v appears as

	v n_1 n_2 ... n_x -1

	As an example, an instance of C_5, the cycle on four vertices, would
	appear as

	<beginning of file>
	0 1 4 -1
	1 2 -1
	2 3 -1
	3 4 -1
	4 -1
	
	<end of file>
	
	
     -s
	Skip the naive scheme and proceed directly to the scaled scheme in each
	experiment.  This option generally causes the algorithm to take longer,
	but it is recommended when using an input clustering from a file.


     -c num
	Allow no more than "num" clusters.  "num" must be between 2 and n,
	where n is the number of vertices in the graph.  If the -c option is
	not specified or an invalid value is given, n clusters are used.


     -t num
	Set the tabu length to "num".  Default value is 1.  Note that when the
	-T option is used, vertices can appear on the tabu list more than once
	and moving them is only forbidden when they are on the tabu list more
	than TabuTol times, where TabuTol is the tabu list tolerance.


     -T num
	Set the tabu list tolerance to "num".  Default value is 1.  The tabu
	list tolerance is the number of times a vertex must appear on the tabu
	list before moving it is forbidden.


     -n num
	Set the naive stopping tolerance to "num".  Default value is 5.  This
	is the number of steps that the naive scheme will continue without
	improving the best cost.  If you run the scaled scheme, using a higher
	naive stopping tolerance isn't likely to improve your results.


     -N num
	Set the scaled stopping tolerance to "num".  Default value is 5.  This
	is the number of steps that the scaled scheme will continue without
	improving the best cost.  Setting the tolerance to 0 will cause the
	algorithm to skip the scaled scheme.


     -e num
	Run "num" experiments.  The best final clustering over all experiments
	will be written to file.  Default is 1.


     -D num
	Set the diversification frequency to "num".  Without this option, no
	diversification will be performed.  If the -d flag is also used, then
	"num" is the shuffling diversification frequency.  If the -d flag is
	not used, then "num" is the destructive diversification frequency.  It
	is recommended that the -d flag is used, because in the fast version of
	RNSC (i.e. this one), destructive diversification isn't much help.


     -d num
	Set the shuffling diversification length to "num".  That means that the
	last "num" moves in the diversification period will be diversification
	moves.  Don't set this to be higher than the diversification frequency.


     -i input_clustering_filename
	Read an initial clustering from file.  This file must have the same
	format as an RNSC output clustering file.  That is, each cluster is a
	list of vertices, terminated with a -1 token.  By default, the initial
	clustering is random.


     -o output_clustering_filename
	Write the final clustering to a given filename.  Default is out.rnsc.


EXAMPLE

	rnsc -g ../graphs/11.gra -e3 -iin.rnsc -oout.rnsc -n2 -N100 -D40 -d10
	-c300 -t15 -T2


SEE ALSO

	Andrew D. King and Rudi Mathon.  A fast cost-based graph clustering
	algorithm.  Manuscript.

	Andrew D. King.  Graph Clustering with Restricted Neighbourhood Search.
	M. Sc. thesis, University of Toronto.  2004.

	Andrew D. King, Natasa Przulj, and Igor Jurisica.  Protein complex
	prediction via cost-based clustering.  Bioinformatics Advance Access.
	2004.
