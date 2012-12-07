#include <fstream>
#include <vector>
#include <string>

using namespace std;

int main() {
     ifstream fin;
     ofstream fout;
     fout.open("fourLetterDictionary.txt");
     fin.open("dictionary.txt");
     
     string temp;
     while(!fin.eof()) {
          getline(fin, temp);
          if(temp.size() == 5) {
               fout << temp << endl;
          }
     }
}
