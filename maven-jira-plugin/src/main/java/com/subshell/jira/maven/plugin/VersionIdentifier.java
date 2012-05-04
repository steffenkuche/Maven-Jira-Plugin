package com.subshell.jira.maven.plugin;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class VersionIdentifier implements Serializable, Comparable<VersionIdentifier> {
	private static final long serialVersionUID = 7629341513817576527L;

	private final int major;
	private final int minor;
	private final int service;
	private final String qualifier;

	public VersionIdentifier(int major, int minor, int service) {
		this(major, minor, service, null);
	}
	
	public VersionIdentifier(int major, int minor) {
		this(major, minor, 0, null);
	}
	
	public VersionIdentifier(int major, int minor, String qualifier) {
		this(major, minor, 0, qualifier);
	}
	
	public VersionIdentifier(int major) {
		this(major, 0, 0, null);
	}
	
	public VersionIdentifier(int major, String qualifier) {
		this(major, 0, 0, qualifier);
	}
	
	public VersionIdentifier(int major, int minor, int service, String qualifier) {		
		if (major < 0 ||  minor < 0 || service < 0) {
			throw new IllegalArgumentException("invalid version number");
		}
				
		this.major = major;
		this.minor = minor;
		this.service = service;
		this.qualifier = qualifier;
	}
	
	public static VersionIdentifier createFromString(String version) {
		if (StringUtils.isBlank(version)) {
			throw new IllegalArgumentException("invalid version number");
		}
		String[] parts = StringUtils.split(version, '.');
		
		String qualifier = null;
		String[] qualifierParts = StringUtils.split(parts[parts.length - 1], '-');
		if (qualifierParts.length > 1) {
			qualifier = qualifierParts[1];
			parts[parts.length - 1] = qualifierParts[0];
		}
		
		int major = Integer.parseInt(parts[0]);

		int minor = 0;
		if (parts.length > 1) {
			minor =  Integer.parseInt(parts[1]);
		}
		
		int service = 0;
		if (parts.length > 2) {
			service =  Integer.parseInt(parts[2]);
		}
		
		return new VersionIdentifier(major, minor, service, qualifier);
	}
	
	public int compareTo(VersionIdentifier other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder()
			.append(major, other.major)
			.append(minor, other.minor)
			.append(service, other.service);
		
		return compareToBuilder.toComparison();
	}
	
	public boolean isVersionEqual(VersionIdentifier other) {
		int comparisonResult = compareTo(other);
		return comparisonResult == 0;
	}
	
	public boolean isVersionLessThan(VersionIdentifier other) {
		int comparisonResult = compareTo(other);
		return comparisonResult < 0;
	}
	
	public boolean isVersionLessThanOrEquals(VersionIdentifier other) {
		int comparisonResult = compareTo(other);
		return comparisonResult <= 0;
	}
	
	public boolean isVersionGreaterThan(VersionIdentifier other) {
		int comparisonResult = compareTo(other);
		return comparisonResult > 0;
	}
	
	public boolean isVersionGreaterThanOrEquals(VersionIdentifier other) {
		int comparisonResult = compareTo(other);
		return comparisonResult >= 0;
	}
	
	public boolean isSnapshot() {
		return StringUtils.equalsIgnoreCase(qualifier, "SNAPSHOT");
	}

	public String toStringWithoutQualifier() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.').append(minor);
		sb.append('.').append(service);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.').append(minor);
		sb.append('.').append(service);
		if (qualifier != null) {
			sb.append('-').append(qualifier);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!o.getClass().equals(getClass())) {
			return false;
		}

		VersionIdentifier other = (VersionIdentifier) o;
		EqualsBuilder equalsBuilder  = new EqualsBuilder()
			.append(major, other.major)
			.append(minor, other.minor)
			.append(service, other.service)
			.append(qualifier, other.qualifier);
		return equalsBuilder.isEquals();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder()
			.append(major)
			.append(minor)
			.append(service)
			.append(qualifier);
		return hash.toHashCode();
	}

	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public int getService() {
		return service;
	}
	
	public String getQualifier() {
		return qualifier==null?"":qualifier;
	}

}
