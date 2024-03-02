import tempfile
import json
import os
import os.path
import sys
from subprocess import Popen, PIPE, STDOUT

USAGE = 'python {} <json file> <shell to test> [-v]'.format(sys.argv[0])
isVerbose = False

def customPrint(*args, alwaysPrint=False):
    if alwaysPrint:
        print(*args)
    elif isVerbose:
        print(*args)

def isSame(expected, actual):
    if expected != actual:
        customPrint("expected:\t", expected, alwaysPrint=True)
        customPrint("actual:\t\t", actual, alwaysPrint=True)
        return False
    return True

def joinToByteString(data):
    byteStr = b""
    for string in data:
        byteStr += os.linesep.encode() + string.encode()
    return byteStr.split(os.linesep.encode(), 1)[1] if byteStr else byteStr

def createTestFiles(tmpdirname, data):
    for dirName in data['dirToCreate']:
        newpath = os.path.join(tmpdirname, dirName)
        if not os.path.exists(newpath):
            os.makedirs(newpath)
    for fileName, fileContent in data['fileToCreate'].items():
        fileContent = joinToByteString(fileContent)
        newpath = os.path.join(tmpdirname, fileName)
        with open(newpath, 'wb') as f:
            f.write(fileContent) 

def validateOutput(testCase, data, stdout, stderr):
    valid = {
        'output': True,
        'error': True,
        'file content': True
    }
    if len(data['expOutput']) != 0:
        expOutput = joinToByteString(data['expOutput'])
        if not isSame(expOutput, stdout):
            valid['output'] = False
    if len(data['expError']) != 0:
        expError = joinToByteString(data['expError'])
        if not isSame(expError, stderr):
            valid['error'] = False
    if len(data['expFileContent']) != 0:
        for fileName, expectedContent in data['expFileContent'].items():
            expectedContent = joinToByteString(expectedContent)
            actualContent = b""
            try:
                with open(fileName, 'rb') as f:
                    actualContent = f.read()
                if not isSame(expectedContent, actualContent):
                    valid['file content'] = False
            except:
                customPrint("unable to open expected file:", fileName, alwaysPrint=True)
                valid['file content'] = False

    allPass = True
    for check, outcome in valid.items():
        if not outcome:
            allPass = False
            customPrint(check, "is not the same as expected", check,  alwaysPrint=True)

    if allPass:
        customPrint(testCase, ": Passed!")
        return True
    else:
        customPrint(testCase, ": Failed!", alwaysPrint=True)
        customPrint("="*50, alwaysPrint=True)
        return False

def main(testJson, shell):
    testScenarios = json.load(open(testJson))
    cwd = os.getcwd()
    if shell.endswith("jar"):
        shell = "java -jar " +  os.path.realpath(shell)
    failed = 0
    for data in testScenarios:
        testCase = data['testName']
        customPrint("="*50)
        customPrint("Executing test case:", testCase)
        with tempfile.TemporaryDirectory() as tmpdirname:
            try:
                createTestFiles(tmpdirname, data)
                os.chdir(tmpdirname)
                p = Popen(shell.split(), stdin=PIPE, stdout=PIPE, stderr=PIPE)
                cmdString = joinToByteString(data['testCommand'])
                customPrint("Running the following commands:")
                customPrint(cmdString.decode())
                stdout, stderr = p.communicate(input=cmdString)

                passed = validateOutput(testCase, data, stdout, stderr)
                if not passed:
                    failed += 1
            except Exception as e:
                customPrint("Error:", e, alwaysPrint=True)
                customPrint(testCase, ": Failed!", alwaysPrint=True)
                customPrint("="*50, alwaysPrint=True)
                continue
            finally:
                os.chdir(cwd)
        customPrint()
    if failed == 0:
        customPrint("Ran {} test cases and all passed!".format(len(testScenarios)), alwaysPrint=True)
    else:
        customPrint("Ran {} test cases and {} failed!".format(len(testScenarios), failed), alwaysPrint=True)

if __name__ == '__main__':
    if (len(sys.argv) < 3):
        print(USAGE)
        sys.exit(1)
    if len(sys.argv) > 3 and sys.argv[3] == "-v":
        isVerbose = True
    main(sys.argv[1], sys.argv[2])

