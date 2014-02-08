/*******************************************************************************/
/* Description : Implementation of 4 methods of graph processing		       */
/* Author      : Charalampos Moschopoulos, IIBEAA                              */
/* Last update : 06/04/2008                                                    */
/*******************************************************************************/
#define MAXLINE 100
#define STREND '\0'

#define TRUE       1
#define FALSE      0

 

struct Edge {
 struct Node *source;
 struct Node *partner;
 struct Edge *next;
 struct Edge *prev;
  int weight;
};

struct Node {
    struct Edge *edges;
	char *label;
	int part;
	int degree;
	int origdeg;
	int weight;
	struct Node *next;
	struct Node *prev;
};

struct Graph {
	char *name;
	struct Node *nodes;
	int nodecount;
	int edgecount;
	int maxdegree;
	int size;
};


struct SigDense {
  Graph *sg;
  struct SigDense *prev;
  struct SigDense *next;  
};

struct SigDenseSet {
  int size;
  SigDense *first;
} ;
