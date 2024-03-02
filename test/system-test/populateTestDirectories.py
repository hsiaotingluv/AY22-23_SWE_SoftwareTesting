import json
import os
import os.path

data = {
    "dirToCreate": ["testFolder1", "testFolder1/testdir", "testFolder1/anotherdir", "testFolder1/-dashdir", "testFolder1/anotherdir/fundir", "testFolder1/anotherdir/lessfundir", "testFolder1/special", "testFolder1/folderwithempty", "testFolder1/folderwithempty/folderwithmoreempty", "testFolder1/folderwithempty/folderwithmoreempty/empty2", "testFolder1/folderwithempty/empty1", "testFolder1/folderwithempty/empty3"],
    "fileToCreate": {
        "testFolder1/test.txt": ["This is the content for test file 1.\nNothing much here\nwow\nhaha\n123"],
        "testFolder1/helloworld.txt": ["hello world!\nbasic file\n101"],
        "testFolder1/popcorn.java": ["hmm\nokay\nhaha\n"],
        "testFolder1/testdir/test": ["this is test\n321"],
        "testFolder1/testdir/test1": ["1234\nthis is test1\n"],
        "testFolder1/testdir/test2": ["test2 this is\nlalala"],
        "testFolder1/testdir/example.txt": ["an example text file\n"],
        "testFolder1/anotherdir/anothertest": ["12345"],
        "testFolder1/anotherdir/anothertest1": ["1234"],
        "testFolder1/anotherdir/fundir/basic": ["basic cisab\n1\n2\n4\n9"],
        "testFolder1/anotherdir/fundir/cs4218": ["CS4218\ncs4218\n"],
        "testFolder1/anotherdir/fundir/file.txt": ["wow a file\n1 2 3\n3 2 1"],
        "testFolder1/anotherdir/lessfundir/cs0000": ["cs0000\nCS0000\neverything\neverywhere\nall\nat\nonce\n!"],
        "testFolder1/anotherdir/lessfundir/lessbasic": ["a less basic file that contains everything but\nnothing"],
        "testFolder1/anotherdir/lessfundir/notafile": ["trick file\n123\n321\n12839\n328123\ntest\nTEST"],
        "testFolder1/-dashdir/-dashfile": ["Wow a dash!\nplease work\n"],
        "testFolder1/-dashdir/--": ["More dashes\n--"],
        "testFolder1/-dashdir/--doubledash": ["double dashes\n--\n----"],
        "testFolder1/special/@special.txt": ["Special characters...\n!@#\n"],
        "testFolder1/special/(brackets)": ["Special characters...\n!@#\n"],
        "testFolder1/special/$dollar": ["$$$$$ file\n!@#\n"],
        "testFolder1/special/+plus+": ["+ many plus\n!@#\n"],
        "testFolder1/special/`backquotes`": ["```\n!@#\n"],
        "testFolder1/folderwithempty/file.txt": ["just for fun!"],
        "testFolder1/folderwithempty/folderwithmoreempty/file1.txt": ["just for fun 2!"]
    }
}

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

if __name__ == '__main__':
    createTestFiles(".", data)