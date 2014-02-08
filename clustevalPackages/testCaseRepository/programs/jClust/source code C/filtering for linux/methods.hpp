/*******************************************************************************/
/* Description : Implementation of 4 methods of graph processing		       */
/* Author      : Charalampos Moschopoulos, IIBEAA                              */
/* Last update : 06/04/2008                                                    */
/*******************************************************************************/

SigDenseSet *initSigDenseSet(){		// arxikopoiisi tis listas me tous piknous subgraphs
	SigDenseSet *sds = new SigDenseSet();
	sds->first  = new SigDense();

	sds->size        = 0;
	sds->first->prev = NULL;
	sds->first->next = NULL;
	sds->first->sg   = NULL;
	return sds;
}

void insertSigDense(SigDenseSet *sds, SigDense *sd){ //eisagogi neou piknou subgraph stin lista
  SigDense *next, *prev;
  //cout<<"Recording significantly dense subgraph: " << sd->sg->nodecount<<" nodes, "<< sd->sg->edgecount/2 << " edges."<<endl;
  prev = NULL;
  for (next = sds->first; next != NULL; next=next->next){
    prev = next;
  } 
  sd->prev = prev;
  sd->next = next;
  prev->next = sd;
  (sds->size)++;
}


SigDenseSet * hairCut(int flag, char *cluster_filename, int par_haircut,SigDenseSet *sds){
  Graph *g;
  Node *node;
  SigDense *sd;
  int r,i;
  	char line[MAXLINE];
	char clusterName[10];
	char edgesNumber[10];
	char dump[10];
  Node **removelist = new Node*[1000];
  for(r=0;r<1000;r++){
	  removelist[r]=NULL;
  }

  if(flag==0){ //einai i proti methodos pou efarmozetai, diavasma apo to intermediate arxeio
	  sds = initSigDenseSet();
		FILE *fp = fopen(cluster_filename, "r");
		if(fp == NULL){
			 cout<<"File "<<cluster_filename<< " could not be opened in density function"<<endl;
			 system("pause");
		}

		while(fgets(line, MAXLINE, fp)){

			if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
			sscanf (line,"%s %s %s",clusterName,dump,edgesNumber);  //diavazei tin proti grammi tou kathe cluster
			g=readCluster(fp, edgesNumber);							//(format:#cluster : #edges)

			r = 0;												//vriskei poies korifes tou cluster exoun mikrotero degree
			for (node = g->nodes; node != NULL; node = node->next){
				if (node->degree < par_haircut){
				removelist[r++] = node;//cout<<"eppppp "<< node->label<<endl;system("pause");
				}
			}

			if (r){								//an iparxoun nodes sto removelist...svistous!
				for (i=0; i<r; i++)
				removeNode(g, removelist[i]);
			}
			if(g->nodecount!=0){
			SigDense *sd = new SigDense();		// apothikeuse to ipoloipo ton clusters sti lista
			sd->sg       = g;
			insertSigDense(sds, sd);
			}
						
		}
		fclose (fp);
	}
  	else if(flag==1){			//den einai i proti methodos pou efarmozetai, opote idi exei dimiourgithei lista sds
		for (sd = sds->first->next; sd != NULL; sd=sd->next){
			r = 0;												//vriskei poies korifes tou cluster exoun mikrotero degree
			for (node = sd->sg->nodes; node != NULL; node = node->next){
				if (node->degree < par_haircut){
				removelist[r++] = node;//cout<<"eppppp "<< node->label<<endl;system("pause");
				}
			}

			if (r){								//an iparxoun nodes sto removelist...svistous!
				for (i=0; i<r; i++)
				removeNode(sd->sg, removelist[i]);
			}
			if(g->nodecount==0){
				sd->prev->next=sd->next;			// an 0 nodes, parekampse ton. PROSOXI!!dimiourgia skoupidion(den svino)
				if(sd->next!=NULL)
				sd->next->prev=sd->prev;
			}
		}
	}
	delete removelist;

	return sds;
}


SigDenseSet * density(int flag, char *cluster_filename, float par_density,SigDenseSet *sds){
	Graph *g;
	SigDense *sd;
	char line[MAXLINE];
	char clusterName[10];
	char edgesNumber[10];
	char dump[10];
	float dense;
	

	if(flag==0){	//einai i proti methodos pou efarmozetai, diavasma apo to intermediate arxeio
		sds = initSigDenseSet();
		FILE *fp = fopen(cluster_filename, "r");
		if(fp == NULL){
			 cout<<"File "<<cluster_filename<< " could not be opened in density function"<<endl;
			 system("pause");
		}

		while(fgets(line, MAXLINE, fp)){

			if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
			sscanf (line,"%s %s %s",clusterName,dump,edgesNumber);  //diavazei tin proti grammi tou kathe cluster
			g=readCluster(fp, edgesNumber);							//(format:#cluster : #edges)

			dense= (float)g->edgecount/(g->nodecount*(g->nodecount-1));
			
		/*	if(dense>=par_density){*/					//elegxos an einai piknos i oxi o cluster.
				SigDense *sd = new SigDense();		// an nai apothikeuse allios katestrepse
				sd->sg       = g;
				insertSigDense(sds, sd);
			/*}
			else{
				destroyGraph(g);
			}*/
		}
		fclose (fp);
	}
	else if(flag==1){			//den einai i proti methodos pou efarmozetai, opote idi exei dimiourgithei lista sds
		for (sd = sds->first->next; sd != NULL; sd=sd->next){
			if(sd->sg->nodecount>2){			//elegxos gia to plithos ton korifon
				dense= (float)sd->sg->edgecount/(sd->sg->nodecount*(sd->sg->nodecount-1));
			}
			else{
				dense=0;
			}
			if(dense<par_density){					//elegxos an einai piknos i oxi o cluster.
				sd->prev->next=sd->next;			// an oxi, parekampse ton. PROSOXI!!dimiourgia skoupidion(den svino)
				if(sd->next!=NULL)
				sd->next->prev=sd->prev;
			}
		}
	}
	else{
		cout<<"The flag is WRONG in density function"<<endl;
		system("pause");
	}
	return sds;
}

			

SigDenseSet * outInside(int flag,char *initial_filename,char *cluster_filename,float par_out_in,SigDenseSet *sds){
	Graph *g,*tg;
	SigDense *sd;
	Node *node, *tnode;
	char line[MAXLINE];
	char clusterName[10];
	char edgesNumber[10];
	char dump[10];
	int orig_degree=0;

	g=readGraph(initial_filename);	//kataskeui tou arxikou grafou
	

	if(flag==0){	//einai i proti methodos pou efarmozetai, diavasma apo to intermediate arxeio
		sds = initSigDenseSet();
		FILE *fp = fopen(cluster_filename, "r");
		if(fp == NULL){
			 cout<<"File "<<cluster_filename<< " could not be opened in density function"<<endl;
			 system("pause");
		}

		while(fgets(line, MAXLINE, fp)){

			if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
			sscanf (line,"%s %s %s",clusterName,dump,edgesNumber);  //diavazei tin proti grammi tou kathe cluster
			tg=readCluster(fp, edgesNumber);							//(format:#cluster : #edges)
//cout<<"elegxos tou cluster  "<<tg->nodes->label<<endl;
			for(tnode=tg->nodes;tnode!=NULL;tnode=tnode->next){		//ipologismos tou sinolikou degree ton korifon pou apoteloun ton cluster
				for(node=g->nodes;node!=NULL;node=node->next){	//
					if(strcmp(node->label,tnode->label)==0){
						orig_degree+=node->degree;
						break;
					}
				}
			}
//cout<<"exoume kai leme: orig_degree= "<<orig_degree<<"   kai ta in= "<<tg->edgecount<<endl<<(float)tg->edgecount/(float)orig_degree<<endl;
			if((float)(tg->edgecount/(float)orig_degree)>=par_out_in){					//elegxos an einai piknos i oxi o cluster.
				SigDense *sd = new SigDense();		// an nai apothikeuse allios katestrepse
				sd->sg       = tg;
				insertSigDense(sds, sd);
				orig_degree=0;
			}
			else{
				destroyGraph(tg);
				orig_degree=0;
			}
		}
		fclose (fp);
	}
	else if(flag==1){				//den einai i proti methodos pou efarmozetai, opote idi exei dimiourgithei lista sds
		
		for (sd = sds->first->next; sd != NULL; sd=sd->next){

			for(tnode=sd->sg->nodes;tnode!=NULL;tnode=tnode->next){		//ipologismos tou sinolikou degree ton korifon pou apoteloun ton cluster
				for(node=g->nodes;node!=NULL;node=node->next){	//
					if(strcmp(node->label,tnode->label)==0){
						orig_degree+=node->degree;
						break;
					}
				}
			}
			if(sd->sg->nodecount<2 || (float)sd->sg->edgecount/orig_degree<par_out_in){			//elegxos 
				sd->prev->next=sd->next;			// an oxi, parekampse ton. PROSOXI!!dimiourgia skoupidion(den svino)
				if(sd->next!=NULL)
				sd->next->prev=sd->prev;
			}
			orig_degree=0;

		}
	}
	else{
		cout<<"The flag is WRONG in density function"<<endl;
		system("pause");
	}
	return sds;
}


SigDenseSet * bestneighbor(int flag,char *initial_filename,char *cluster_filename,float par_bestneighb,SigDenseSet *sds){
	Graph *g,*tg;
	SigDense *sd;
	Node *node, *tnode,*nnode,*tnode2;
	char line[MAXLINE];
	char clusterName[10];
	char edgesNumber[10];
	char dump[10];
	int orig_degree=0,r,numb_neigh,exist,good_neigh;
	float score;
	Edge *edge,*nedge;

	g=readGraph(initial_filename);	//kataskeui tou arxikou grafou

	Node **neighborlist = new Node*[g->nodecount];
  for(r=0;r<g->nodecount;r++){
	  neighborlist[r]=NULL;
  }
	

	if(flag==0){	//einai i proti methodos pou efarmozetai, diavasma apo to intermediate arxeio
		sds = initSigDenseSet();
		FILE *fp = fopen(cluster_filename, "r");
		if(fp == NULL){
			 cout<<"File "<<cluster_filename<< " could not be opened in density function"<<endl;
			 system("pause");
		}

		while(fgets(line, MAXLINE, fp)){

			if (line[strlen(line)-1] == '\n')
			  line[strlen(line)-1] = STREND;
			sscanf (line,"%s %s %s",clusterName,dump,edgesNumber);  //diavazei tin proti grammi tou kathe cluster
			tg=readCluster(fp, edgesNumber);							//(format:#cluster : #edges)

			numb_neigh=0;exist=0;score=0;good_neigh=0;
			for(tnode=tg->nodes;tnode!=NULL;tnode=tnode->next){		//euresi ton geitonon ston arxiko grafo
				for(node=g->nodes;node!=NULL;node=node->next){	//
					if(strcmp(node->label,tnode->label)==0){
						for(edge=node->edges;edge!=NULL;edge=edge->next){
							nnode=edge->partner;
							for(r=0;r<numb_neigh;r++){//elegxos mipos exei xanasinatithei o geitonas
								if(strcmp(neighborlist[r]->label,nnode->label)==0){
									exist=1;
									break;
								}
							}
							for(tnode2=tg->nodes;tnode2!=NULL;tnode2=tnode2->next){//elegxos mipos o geitonas anikei ston grafo
										if(strcmp(nnode->label,tnode2->label)==0){
											exist=1;
											break;
										}
							}
							if(exist==0){//an oxi, ton prosthetoume ston pinaka geitonon kai vlepoume an einai best
								neighborlist[numb_neigh]=nnode;
								for(nedge=neighborlist[numb_neigh]->edges;nedge!=NULL;nedge=nedge->next){//ipologismos ton "kalon" edges
									for(tnode2=tg->nodes;tnode2!=NULL;tnode2=tnode2->next){
										if(strcmp(nedge->partner->label,tnode2->label)==0){
											score++;
											break;
										}
									}
								}
								if(score/neighborlist[numb_neigh]->degree>=par_bestneighb){//an mas kanei, vazoume part=1
									neighborlist[numb_neigh]->part=1;
									good_neigh++;
								//	cout<<"enas kalos komvos einai o: "<< neighborlist[numb_neigh]->label<< "me degree"<<neighborlist[numb_neigh]->degree<<endl;system("pause");
								}								
								numb_neigh++;
								score=0;
							}
							exist=0;
						}
						break;
					}
				}
			}
			
			for(r=0;r<numb_neigh;r++){			//prosthetoume tous kalous geitones ston cluster
				if(neighborlist[r]->part==1){
					Node *new_node;
					new_node=addNode(tg,neighborlist[r]->label);
				}
			}

			tnode=tg->nodes;
			for(r=0;r<good_neigh;r++){//cout<<"ok "<<tnode->label<<endl;system("pause");		// prosthetoume tis edges gia tous kainourgious komvous
				for(node=g->nodes;node!=NULL;node=node->next){//cout<<"ok2"<<endl;system("pause");	//
					if(strcmp(node->label,tnode->label)==0){//cout<<"ok3"<<endl;system("pause");
						for(edge=node->edges;edge!=NULL;edge=edge->next){//cout<<"ok4"<<endl;system("pause");
							for(tnode2=tg->nodes;tnode2!=NULL;tnode2=tnode2->next){//cout<<"ok5"<<endl;system("pause");
								if(strcmp(edge->partner->label,tnode2->label)==0){//cout<<"ok6"<<endl;system("pause");
									addEdge(tg,tnode,tnode2,edge->weight);
									addEdge(tg,tnode2,tnode,edge->weight);
								}
							}
						}
						break;
					}
				}
				tnode=tnode->next;
			}

			if(g->nodecount!=0){
			SigDense *sd = new SigDense();		// apothikeuse to ipoloipo ton clusters sti lista
			sd->sg       = tg;
			insertSigDense(sds, sd);
			}
		}
		fclose(fp);
	}
	else if(flag==1){			//den einai i proti methodos pou efarmozetai, opote idi exei dimiourgithei lista sds
		
		for (sd = sds->first->next; sd != NULL; sd=sd->next){
		numb_neigh=0;exist=0;score=0;good_neigh=0;
			for(tnode=sd->sg->nodes;tnode!=NULL;tnode=tnode->next){		//euresi ton geitonon ston arxiko grafo
				for(node=g->nodes;node!=NULL;node=node->next){	//
					if(strcmp(node->label,tnode->label)==0){
						for(edge=node->edges;edge!=NULL;edge=edge->next){
							nnode=edge->partner;
							for(r=0;r<numb_neigh;r++){//elegxos mipos exei xanasinatithei o geitonas
								if(strcmp(neighborlist[r]->label,nnode->label)==0){
									exist=1;
									break;
								}
							}
							for(tnode2=sd->sg->nodes;tnode2!=NULL;tnode2=tnode2->next){//elegxos mipos o geitonas anikei ston grafo
										if(strcmp(nnode->label,tnode2->label)==0){
											exist=1;
											break;
										}
							}
							if(exist==0){//an oxi, ton prosthetoume ston pinaka geitonon kai vlepoume an einai best
								neighborlist[numb_neigh]=nnode;
								for(nedge=neighborlist[numb_neigh]->edges;nedge!=NULL;nedge=nedge->next){//ipologismos ton "kalon" edges
									for(tnode2=sd->sg->nodes;tnode2!=NULL;tnode2=tnode2->next){
										if(strcmp(nedge->partner->label,tnode2->label)==0){
											score++;
											break;
										}
									}
								}
								if(score/neighborlist[numb_neigh]->degree>=par_bestneighb){//an mas kanei, vazoume part=1
									neighborlist[numb_neigh]->part=1;
									good_neigh++;
								//	cout<<"enas kalos komvos einai o: "<< neighborlist[numb_neigh]->label<< "me degree"<<neighborlist[numb_neigh]->degree<<endl;system("pause");
								}								
								numb_neigh++;
								score=0;
							}
							exist=0;
						}
						break;
					}
				}
			}
			
			for(r=0;r<numb_neigh;r++){			//prosthetoume tous kalous geitones ston cluster
				if(neighborlist[r]->part==1){
					Node *new_node;
					new_node=addNode(sd->sg,neighborlist[r]->label);
				}
			}

			tnode=sd->sg->nodes;
			for(r=0;r<good_neigh;r++){//cout<<"ok "<<tnode->label<<endl;system("pause");		// prosthetoume tis edges gia tous kainourgious komvous
				for(node=g->nodes;node!=NULL;node=node->next){//cout<<"ok2"<<endl;system("pause");	//
					if(strcmp(node->label,tnode->label)==0){//cout<<"ok3"<<endl;system("pause");
						for(edge=node->edges;edge!=NULL;edge=edge->next){//cout<<"ok4"<<endl;system("pause");
							for(tnode2=sd->sg->nodes;tnode2!=NULL;tnode2=tnode2->next){//cout<<"ok5"<<endl;system("pause");
								if(strcmp(edge->partner->label,tnode2->label)==0){//cout<<"ok6"<<endl;system("pause");
									addEdge(sd->sg,tnode,tnode2,edge->weight);
									addEdge(sd->sg,tnode2,tnode,edge->weight);
								}
							}
						}
						break;
					}
				}
				tnode=tnode->next;
			}
		}
	}
	else{
		cout<<"The flag is WRONG in density function"<<endl;
		system("pause");
	}
	delete neighborlist;
	return sds;
}




				



								

















	