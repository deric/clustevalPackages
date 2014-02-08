/*******************************************************************************/
/* Description : Implementation of 4 methods of graph processing		       */
/* Author      : Charalampos Moschopoulos, IIBEAA                              */
/* Last update : 06/04/2008                                                    */
/*******************************************************************************/
#include <math.h>

void initEdge(Edge *nedge){
		nedge->source=NULL;
		nedge->partner=NULL;
		nedge->next=NULL;
		nedge->prev=NULL;
		
}

	
void addEdge(Graph *g, Node *node1, Node *node2, int weight){//prosthesi mias kainourgias edge 
		Edge *nedge,*tedge;
		nedge= new Edge();
		initEdge(nedge);				// DESMEUSI MNIMIS!!!!!!
		
		for (tedge=node1->edges; tedge!=NULL; tedge=tedge->next){ //elegxos iparxis tis edge
			if (tedge->partner == node2)
				return;
		}
		//arxikopoisi ton metavliton tin kainourgias edge
		nedge->source=node1;
		nedge->partner=node2;
		nedge->weight=weight;
		//arxikopoiisi ton deikton tis kainourgias edge kai tis listas tou komvou
		if (node1->edges != NULL)
			node1->edges->prev=nedge;              //min xehname ton teleutaio deikti
		nedge->prev=NULL;
		nedge->next=node1->edges;
		node1->edges=nedge;
		//allagi metavliton tou Node
		(node1->degree)++;
		g->maxdegree = (g->maxdegree>node1->degree)?g->maxdegree:node1->degree;
		(g->edgecount)++;
}





void Initial_Node(Node *node){			//arxikopoiisi timon enos kainourgiou node(default constructor)
		node->edges=NULL;
		node->label= new char[25];			//DESMEUSI MNIMIS!!!
		strcpy(node->label,"NOT DEFINED");
		node->part=0;
		node->degree=0;
		node->origdeg=0;
		node->weight=1;
		node->next=NULL;
		node->prev=NULL;
}

Graph *Init_Graph(Graph *g){
		g=new Graph();
		g->nodes= NULL;
		g->nodes=NULL;
		g->nodecount=0;
		g->edgecount=0;
		g->maxdegree=0;
		g->size=0;
		return g;
}


void node_display(Node *node){
		Edge *temp;
		cout<< "o komvos exei onoma: "<<node->label<<"me vathmo : "<<node->degree<<" kai original: "<<node->origdeg<<" kai oi akmes tou einai oi : "<<endl;
		for(temp=node->edges; temp!=NULL; temp=temp->next){
			cout<< temp->source->label << " - " << temp->partner->label<<" - " <<temp->weight<<endl;
			cout<<"----------------"<<endl;
		}
}

void Graph_display(Graph *g){
		Node *temp;
	
		cout<<"o grafos g exei "<<g->nodecount<<" korifes kai "<< g->edgecount/2<< "akmes"<<endl;
		for(temp=g->nodes;temp!=NULL;temp=temp->next){
			node_display(temp);
		}
}



Node *addNode(Graph *g,char p[]){//prosthesi enos kainourgiou Node
		Node *nnode;
		nnode= new Node();		// DESMEUSI MNIMIS!!!!!!!!!!!
		Initial_Node(nnode);
		//arxikopoiisi metavliton nnode
		strcpy(nnode->label,p);
		if(g->nodes!=NULL)
			g->nodes->prev=nnode;
		nnode->next=g->nodes;
		nnode->prev=NULL;
		g->nodes=nnode;
		//allagi ton metavliton tou Graph
		(g->nodecount)++;		
		return nnode;
}


void destroyGraph(Graph *g){
	Node *tnode,*tnode2;
	Edge *tedge,*tedge2;

	for(tnode=g->nodes; tnode!=NULL; tnode=tnode->next){         //midenismos ton deikton ton akmon
		for(tedge=tnode->edges; tedge!=NULL; tedge=tedge->next){
			tedge->source=NULL;
			tedge->partner=NULL;
		}
	}

	tnode=g->nodes;
	while(tnode!=NULL){
		tnode2=tnode;
		tedge=tnode->edges;
		while(tedge!=NULL){
			tedge2=tedge;
			tedge=tedge->next;
			tedge2->next=NULL;
			tedge2->prev=NULL;
			delete tedge2;
		}
		tnode=tnode->next;
		tnode2->next=NULL;
		tnode2->prev=NULL;
		delete tnode2->label;
		delete tnode2;
	}
	delete g;
}


Graph *readCluster(FILE *fname, char *edgesNumber){
	int counter;
	Graph *g;
	Node *temp;
	g=Init_Graph(g);
	char line[MAXLINE];
	char label1[25];
	char label2[25];
	char weight[10];

	for(counter=0;counter<atoi(edgesNumber);counter++){

		Node *node1,*node2;
		int flag=0;
		fgets(line, MAXLINE, fname);

		 if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
		sscanf (line,"%s %s %s",label1,label2,weight);     //diavazei grammi grammi to arxeio (format:a pp b)

		if(strcmp(label1,label2)!=0){
			
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exoun xanadiavastei oi nodes
				if(strcmp(label1,temp->label)==0){
					flag+=1;node1=temp;
				}
				if(strcmp(label2,temp->label)==0){
					flag+=2;node2=temp;
				}
			}

			if(flag==0){					//ama einai teleios kainourgoi oi komvoi prosthese tous mazi me tis akmes
				node1=addNode(g,label1);
				node2=addNode(g,label2);
				addEdge(g,node1,node2,atoi(weight));
				addEdge(g,node2,node1,atoi(weight));			

			}
			else if(flag==1){
				node2=addNode(g,label2);
				addEdge(g,node1,node2,atoi(weight));
				addEdge(g,node2,node1,atoi(weight));			
			}
			else if(flag==2){
				node1=addNode(g,label1);
				addEdge(g,node1,node2,atoi(weight));
				addEdge(g,node2,node1,atoi(weight));			
			}
			else if(flag==3){
				addEdge(g,node1,node2,atoi(weight));
				addEdge(g,node2,node1,atoi(weight));
			}
		}
		else{								//ama einai loop i akmi(idio label1 kai label2)
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exei xanadiavastei o komvos 
				if(strcmp(label1,temp->label)==0)
					flag=1;
			}

			if (flag==0){								//an oxi, prosthese to
				addNode(g,label1);
			}
		}
	}


	return g;
}


Graph *readGraph(char *fname){
	Graph *g;
	Node *temp;
	g=Init_Graph(g);
	char line[MAXLINE];
	char label1[25];
	char label2[25];
	char dump[25];

	FILE * pf;							//OPEN INPUT FILE
    pf = fopen (fname,"r");
    if (pf==NULL)
	{
	 cout<<"THE FILE CALLED"<< fname<< " DID NOT FIND"<<endl;
	 system("pause");
	 }

	while (fgets(line, MAXLINE, pf)){
		Node *node1,*node2;
		int flag=0;

		 if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
		sscanf (line,"%s %s %s",label1,label2,dump);     //diavazei grammi grammi to arxeio (format:a pp b)

		if(strcmp(label1,label2)!=0){
			
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exoun xanadiavastei oi nodes
				if(strcmp(label1,temp->label)==0){
					flag+=1;node1=temp;
				}
				if(strcmp(label2,temp->label)==0){
					flag+=2;node2=temp;
				}
			}

			if(flag==0){					//ama einai teleios kainourgoi oi komvoi prosthese tous mazi me tis akmes
				node1=addNode(g,label1);
				node2=addNode(g,label2);
				addEdge(g,node1,node2,1);
				addEdge(g,node2,node1,1);			

			}
			else if(flag==1){
				node2=addNode(g,label2);
				addEdge(g,node1,node2,1);
				addEdge(g,node2,node1,1);			
			}
			else if(flag==2){
				node1=addNode(g,label1);
				addEdge(g,node1,node2,1);
				addEdge(g,node2,node1,1);			
			}
			else if(flag==3){
				addEdge(g,node1,node2,1);
				addEdge(g,node2,node1,1);
			}
		}
		else{								//ama einai loop i akmi(idio label1 kai label2)
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exei xanadiavastei o komvos 
				if(strcmp(label1,temp->label)==0)
					flag=1;
			}

			if (flag==0){								//an oxi, prosthese to
				addNode(g,label1);
			}
		}
	}

	fclose (pf);

	for(temp=g->nodes;temp!=NULL;temp=temp->next){
		temp->origdeg=temp->degree;
	}

	return g;
}


Graph *buildGraph(Graph *g,char ***label,int *weight,int counter){
	Node *temp;
	char label1[25];
	char label2[25];

	int i=0;

	for(i=0; i<counter; i++){//gia oses eggrafes einai mesa ston pinaka label
		Node *node1,*node2;
		int flag=0;
			
		 strcpy(label1,label[i][0]);
		 strcpy(label2,label[i][1]);

		if(strcmp(label1,label2)!=0){
			
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exoun xanadiavastei oi nodes
				if(strcmp(label1,temp->label)==0){
					flag+=1;node1=temp;
				}
				if(strcmp(label2,temp->label)==0){
					flag+=2;node2=temp;
				}
			}

			if(flag==0){					//ama einai teleios kainourgoi oi komvoi prosthese tous mazi me tis akmes
				node1=addNode(g,label1);
				node2=addNode(g,label2);
				addEdge(g,node1,node2,weight[i]);
				addEdge(g,node2,node1,weight[i]);			

			}
			else if(flag==1){
				node2=addNode(g,label2);
				addEdge(g,node1,node2,weight[i]);
				addEdge(g,node2,node1,weight[i]);			
			}
			else if(flag==2){
				node1=addNode(g,label1);
				addEdge(g,node1,node2,weight[i]);
				addEdge(g,node2,node1,weight[i]);			
			}
			else if(flag==3){
				addEdge(g,node1,node2,weight[i]);
				addEdge(g,node2,node1,weight[i]);
			}
		}
		else{								//ama einai loop i akmi(idio label1 kai label2)
			for(temp=g->nodes;temp!=NULL;temp=temp->next){ //elegxos gia an to exei xanadiavastei o komvos
				if(strcmp(label1,temp->label)==0)
					flag=1;
			}

			if (flag==0){								//an oxi, prosthese to
				addNode(g,label1);
			}
		}
	}

	for(temp=g->nodes;temp!=NULL;temp=temp->next){
		temp->origdeg=temp->degree;
	}

	return g;
}


void destroyDense(SigDenseSet *sds){
	SigDense *temp_sd,*sd;
	Graph *temp_g;
	
	for (sd = sds->first->next; sd != NULL; sd=sd->next){ //delete ton grafon ton piknon perioxon
		temp_g=sd->sg;
		sd->sg=NULL;
		destroyGraph(temp_g);
	}

	sd=sds->first->next;
	while(sd!=NULL){			//midenismo ton dikton kai svisimo ton domon sd
		temp_sd=sd;
		sd=sd->next;

		temp_sd->next=NULL;
		temp_sd->prev=NULL;
		delete temp_sd;
	}

	delete sds;
}

Edge *findEdge(Node *source, Node *partner){
  Edge *edge;
  for (edge = source->edges; edge != NULL; edge = edge->next){
    if (edge->partner == partner)
      return edge;
  }
  return NULL;
}


void removeEdge(Graph *g, Edge *edge){
  Node *source = edge->source;
  if (edge->prev == NULL)
    source->edges = edge->next;
  else
    edge->prev->next = edge->next;
  if (edge->next != NULL)
    edge->next->prev = edge->prev;
  edge->prev = NULL;
  edge->next = NULL;
  edge->source=NULL;
  edge->partner=NULL;
  delete edge;
  (source->degree)--;
  (g->edgecount)--;
}


void removeNode(Graph *g, Node *node){
  Edge *tedge,*tedge2;
  tedge=node->edges;
  while(tedge!=NULL){									//svisimo ton akmon pou sindeontai me to node
	  Edge *egde = findEdge(tedge->partner, node);      //pairno tin antistrofi akmi kai tin svino
    removeEdge(g, egde);
	tedge2=tedge;
	tedge=tedge->next;
	tedge2->next=NULL;								//svino tin akmi tou node
	tedge2->prev=NULL;
	tedge2->partner=NULL;
	tedge2->source=NULL;
	delete tedge2;
	(g->edgecount)--;
  }

  if (node->prev == NULL)						//svisimo tou node
    g->nodes = node->next;
  else
    node->prev->next = node->next;
  if (node->next != NULL)
    node->next->prev = node->prev;
  node->next = NULL;
  node->prev = NULL;
  delete node->label;
  delete node;
  (g->nodecount)--;
}




