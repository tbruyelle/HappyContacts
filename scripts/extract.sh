path=../../tmp
file=$path/$1$2.day
log=$path/$1$2.log
echo "processing $file"
wget http://www.ephemeride.name/index.php?jourR=$1-$2 -O $file -o $log
perl -ne '/galement les\.\.\.<.*?>\.(.*?)\./ && print "$1\n"' $file > $file.tmp
perl -i -pe 's/\s//g'  $file.tmp
perl -i -pe 's/-/\n/g' $file.tmp
iconv -f utf-8 -t iso-8859-1 $file.tmp > $file.ok
nb=`wc -l $file.ok`
echo "extracted $nb names..."
for name in `cat $file.ok`
do
        echo "insert into feast (name, day, source) values (\"$name\", \"$1/$2\", \"ephemeride.name\");" >> $path/names.sql
done
rm $path/*.bak
rm $path/*.tmp
exit 0
