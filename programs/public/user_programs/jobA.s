# Job A:
# 1. Create Job B with long execution time
# 2. Create 7 dummy jobs with medium execution time (the creation of the last of these dummy jobs should fail)
# 3. exit

    la a0, job_B        # load address of job_B
    li a1, 200          # run for 200 cycles
    li a7, 221          # create new process
    ecall               # ------- " -------
    li t1, 7            # number of dummy jobs to create
    li a1, 100          # run each dummy job for 100 cycles
    li a7, 221          # syscall number for create process

    la a0, dummy_job    # load address of dummy_job
job_loop:
    la t0, dummy_job    # check whether previous job creation was successful (redundant in first iteration)
    beq t1, zero, exit_job # if no more dummy jobs to create,
    bne a0, t0, failure
    ecall               # create dummy job
    addi t1, t1, -1     # decrement the number of dummy jobs
    j job_loop          # repeat until all dummy jobs are created
exit_job:              
    li t1, -1
    bne t1, a0, failure # if the last dummy job creation didn't fail we have a problem
    li a7, 93           # exit the process
    ecall               # ------- " -------
    
failure:
    j failure