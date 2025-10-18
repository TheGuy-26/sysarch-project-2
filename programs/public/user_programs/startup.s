    la a0, job_A        # load address of job_A
    li a1, 100          # run for 100 cycles
    li a7, 221          # create new process
    ecall               # ------- " -------
    li a7, 93           # exit the process
    ecall               # ------- " -------

