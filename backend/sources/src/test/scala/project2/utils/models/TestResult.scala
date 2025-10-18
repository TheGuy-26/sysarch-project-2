package project2.utils.models

sealed trait TestResult
case object TestNotRun extends TestResult
case object TestSuccess extends TestResult
case object TestFailure extends TestResult
case object TestError extends TestResult
case object TestTimeout extends TestResult

trait TestResultAttribute {
    var result: TestResult = TestNotRun
}