#include <stdio.h>
#include <mpi.h>

int my_send(int id) {
  int revenge;
  printf("Hit from %d\n", id);
  MPI_Send(&id, 1, MPI_INT, 1, 0, MPI_COMM_WORLD);

  MPI_Recv(&revenge, 1, MPI_INT, 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  printf("%d :C\n", revenge);
  printf("RIP BULLY\n");
  return 0;
}

int my_receive(int id) {
  int id_bully;
  MPI_Recv(&id_bully, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

  printf("Ouch!! %d from %d\n", id_bully, id);

  printf("MADAFAKAAAA\n");

  MPI_Send(&id, 1, MPI_INT, 0, 0, MPI_COMM_WORLD);

  return 0;
}

int main(int argc, char* argv[]) {
  int id;

  MPI_Init(&argc, &argv);

  MPI_Comm_rank(MPI_COMM_WORLD, &id);

  if (id == 0) {
    my_send(id);
  } else {
    my_receive(id);
  }

  MPI_Finalize();
  return 0;
}