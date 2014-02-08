/*******************************************************************************/
/* Description : Implementation of 4 methods of graph processing		       */
/* Author      : Charalampos Moschopoulos, IIBEAA                              */
/* Last update : 06/04/2008                                                    */
/*******************************************************************************/
//#include<conio.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>
#include <assert.h>
#include <sys/types.h>
#include<string.h>

//#include <fstream>
#include <iostream>
#include <sstream>
using namespace std;

#include "bibe.hpp"
#include "graph.hpp"
#include "methods.hpp"


void writeSigDenseSet(SigDenseSet *sds, char *fname){
  SigDense *sd;
  int i, j;
  FILE *ofp = fopen(fname, "w");
  if(ofp == NULL){
	  cout<<"File "<<fname<< " could not be opened in writeSigDenseSet"<<endl;system("pause");
  }
  fprintf(ofp, "The EMC algorithm identified the following significantly dense subgraphs\n");
  fprintf(ofp, "Rank\t#Nodes\t#Edges\tNode labels\n");
  i = 0;
  for (sd = sds->first->next; sd != NULL; sd=sd->next){
			Node *node;
			fprintf(ofp, "%d\t%d\t%d\t", ++i, sd->sg->nodecount, sd->sg->edgecount/2);
			j = 1;

			for (node = sd->sg->nodes; node != NULL; node = node->next){
				fprintf(ofp, "%s", node->label);
				printf("%s", node->label);
				if (j < sd->sg->nodecount)
					fprintf(ofp, ", ");
				j++;
			}
			fprintf(ofp, "\n");
  }

  fclose(ofp);
}


int main(int argc, char *argv[]){
  Graph *g;
  SigDenseSet *sds;
  int mode=0;
//i eisodos sto programma prepei na exei 7 orismata kai tin parakato morfi:
// initial_file intermediate_file mode parameter_for_density parameter_for_outside_and_inside_edges parameter_for_neighbors_ parameter_for_haircut
    if (argc != 8)
    {
        fprintf(stderr, "%s initial_file intermediate_file mode par_density par_out_ins par_neighbor par_haircut.\n", argv[0]);
        system("pause");
		exit(1);
    }

	mode=atoi(argv[3]);

	if (mode==0){
		cout << "Wrong mode input: Mode values range from 1 to 15.\n";
		exit(0);
	}
	if (mode==1){ //exei epilexthei o elegxos density
		sds=density(0, argv[2], atof(argv[4]), sds);
	}	
	else if (mode==2){//exei epilexthei o elegxos outside-inside edges
		
		sds=outInside(0,argv[1],argv[2],atof(argv[5]),sds);
	}
	else if (mode==3){//exei epilexthei o elegxos best neighbors
		sds=bestneighbor(0,argv[1],argv[2],atof(argv[6]),sds);
	}
	else if (mode==4){//exei epilexthei o elegxos haircut
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
	}
	else if (mode==5){//exei epilexthei o elegxos density + o elegxos outside-inside edges
		sds=outInside(0,argv[1],argv[2],atof(argv[5]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}
	else if (mode==6){//exei epilexthei o elegxos density + o elegxos best neighbors
		sds=bestneighbor(0,argv[1],argv[2],atof(argv[6]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);		
	}
	else if (mode==7){//exei epilexthei o elegxos haircut+o elegxos density
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}
	else if (mode==8){//exei epilexthei o elegxos outside-inside edges + o elegxos best neighbors
		sds=bestneighbor(0,argv[1],argv[2],atof(argv[6]),sds);
		sds=outInside(1,argv[1],argv[2],atof(argv[5]),sds);
	}
	else if (mode==9){//exei epilexthei o elegxos haircut + o elegxos outside-inside edges
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=outInside(1,argv[1],argv[2],atof(argv[5]),sds);
	}
	else if (mode==10){//exei epilexthei o elegxos haircut + o elegxos best neighbors
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=bestneighbor(1,argv[1],argv[2],atof(argv[6]),sds);
	}
	else if (mode==11){//exei epilexthei o elegxos haircut+o elegxos density + o elegxos outside-inside edges
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=outInside(1,argv[1],argv[2],atof(argv[5]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}
	else if (mode==12){//exei epilexthei o elegxos haircut + o elegxos best neighbors + o elegxos outside-inside edges
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=bestneighbor(1,argv[1],argv[2],atof(argv[6]),sds);
		sds=outInside(1,argv[1],argv[2],atof(argv[5]),sds);
	}
	else if (mode==13){//exei epilexthei o elegxos haircut + o elegxos best neighbors +o elegxos density
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=bestneighbor(1,argv[1],argv[2],atof(argv[6]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}
	else if (mode==14){//exei epilexthei o elegxos best neighbors+ o elegxos outside-inside edges +o elegxos density
		sds=outInside(0,argv[1],argv[2],atof(argv[5]),sds);
		sds=bestneighbor(1,argv[1],argv[2],atof(argv[6]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}
	else if (mode==15){//exei epilexthei o elegxos haircut + o elegxos best neighbors + o elegxos outside-inside edges +o elegxos density
		sds=hairCut(0,argv[2],atoi(argv[7]),sds);
		sds=bestneighbor(1,argv[1],argv[2],atof(argv[6]),sds);
		sds=outInside(1,argv[1],argv[2],atof(argv[5]),sds);
		sds=density(1, argv[2], atof(argv[4]), sds);
	}


	writeSigDenseSet(sds, "final_complexes.txt");//eggrafi ton telikon apotelesmaton sto arxeio final_complexes.txt

	
	destroyDense(sds);
	//cout<<"TELOS!!!!"<<endl;
	return 0;
}
