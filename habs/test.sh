#!/bin/bash

for f in ./platform/*
do
    if [ -d $f ];then
        echo $f;
    fi
done
