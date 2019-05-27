package com.epac.cap.utils;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;

public class Mapper {
	

	
	DozerBeanMapper dozerBeanMapper;

	public <T, U> List<U> map(final List<T> source, final Class<U> destType) {
		dozerBeanMapper = new DozerBeanMapper();
		final List<U> dest = new ArrayList<>();
		try {
			for (T element : source) {
				dest.add(dozerBeanMapper.map(element, destType));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dest;
	}

	public <T, U> U map(final T element, final Class<U> destType) {
		dozerBeanMapper = new DozerBeanMapper();
		U dest = null;
		try {
			dest = dozerBeanMapper.map(element, destType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dest;

	}
	public Mapper() {
		// TODO Auto-generated constructor stub
	}
}
