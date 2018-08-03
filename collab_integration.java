REV="$2"
_change_type=none
_CCOLLAB=/opt/ccollab-cmdline/ccollab
_SVN=/opt/bitnami/subversion/bin/svn
_SVNLOOK=/opt/bitnami/subversion/bin/svnlook
_REPO_HOME=/opt/bitnami/repository/svn
_REPO=$(basename ${REPOS})
_GROUP_GUID=eb0274d760f2016182fb0fa02c1b402b
_TEMPLATE=Minimum 

--New Code review Damien Walsh

_CCOLLAB_CMD_PRE="${_CCOLLAB} \
 --url http://highesteem.us.dell.com:8080/ --user xxx --password xxx \
 --scm subversion --svn-repo-url http://orval.us.dell.com/subversion/${_REPO}/ \
 --svn-exe ${_SVN} --svn-user xxx --svn-passwd xxx admin"
_LOG_HOOKS=/tmp/${_REPO}Hooks.log

ccollab_failed()
{
  echo ERROR: ccollab failed
  echo $(date) : ERROR: ccollab failed for ${REPOS} @ ${REV} on ${1}. | tee -a ${_LOG_HOOKS}
  return 1
}


# Create reviews for required trunk and non-user branch changes
echo ...running post-commit on ${REPOS} @ ${REV} | tee -a ${_LOG_HOOKS}

${_SVNLOOK} dirs-changed ${_REPO_HOME}/${_REPO} -r ${REV} | grep -i "trunk" | grep -iv "trunk/ManifestList"
trunk_change=$?
${_SVNLOOK} dirs-changed ${_REPO_HOME}/${_REPO} -r ${REV} | grep -i "branches" | grep -iv "branches/user/"
branch_change=$?

if [ ${trunk_change} = 0 ] ; then
  _change_type=trunk
elif [ ${branch_change} = 0 ] ; then
  _change_type=branch
fi

if [ "${_change_type}" != "none" ] ; then
  ${_CCOLLAB_CMD_PRE} trigger create-review --review-id-regex "review:\s+(\d+)" ${REV} || ccollab_failed "${_change_type} create-review"
  ${_CCOLLAB_CMD_PRE} review edit last --template ${_TEMPLATE} || ccollab_failed "${_change_type} review edit last --template ${_TEMPLATE}"  ${_CCOLLAB_CMD_PRE} review edit last --group ${_GROUP_GUID} || ccollab_failed "${_change_type} review edit last --group"
fi

echo ...done with post-commit on ${REPOS} @ ${REV} | tee -a ${_LOG_HOOKS}
exit 0