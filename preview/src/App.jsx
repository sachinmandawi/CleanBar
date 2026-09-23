import React, { useState, useEffect } from 'react';
import {
  EyeOff,
  Eye,
  MoreVertical,
  ArrowLeft,
  Mail,
  Copy,
  CheckCircle2,
  Bug,
  Share2,
  ExternalLink,
  User,
  RefreshCw,
  Download
} from 'lucide-react';

function UpiLogo({ className = "w-5 h-5" }) {
  return (
    <svg className={className} viewBox="0 0 64 64" fillRule="evenodd">
      <path d="M55.05 32.542L38.715 0l-18.15 64z" fill="#097939" />
      <path d="M43.433 32.542L27.1 0 8.95 64z" fill="#ed752e" />
    </svg>
  );
}

function BinanceLogo({ className = "w-5 h-5" }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="#F3BA2F">
      <path d="M16.624 13.9202l2.7175 2.7154-7.353 7.353-7.353-7.352 2.7175-2.7164 4.6355 4.6595 4.6356-4.6595zm4.6366-4.6366L24 12l-2.7154 2.7164L18.5682 12l2.6924-2.7164zm-9.272.001l2.7163 2.6914-2.7164 2.7174v-.001L9.2721 12l2.7164-2.7154zm-9.2722-.001L5.4088 12l-2.6914 2.6924L0 12l2.7164-2.7164zM11.9885.0115l7.353 7.329-2.7174 2.7154-4.6356-4.6356-4.6355 4.6595-2.7174-2.7154 7.353-7.353z" />
    </svg>
  );
}

function GithubIcon({ className = "w-5 h-5" }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
    </svg>
  );
}

const DEVELOPER_EMAIL = "sachinmandawi@gmail.com";
const GITHUB_REPO_URL = "https://github.com/sachinmandawi/CleanBar";
const GITHUB_ISSUES_URL = "https://github.com/sachinmandawi/CleanBar/issues";
const UPI_ID = "darkcaptain@ybl";
const BINANCE_UID = "1138545342";
const APP_VERSION = "1.1.0";

export default function App() {
  const [currentPage, setCurrentPage] = useState('main'); // 'main' | 'about'
  const [isHidden, setIsHidden] = useState(false);

  // Update Checker State
  const [isCheckingUpdate, setIsCheckingUpdate] = useState(false);
  const [updateResult, setUpdateResult] = useState(null);
  const [showUpdateModal, setShowUpdateModal] = useState(false);

  // Floating Bottom Toast Notification (Neeche me Toast)
  const [bottomToast, setBottomToast] = useState(null);

  const showToast = (message) => {
    setBottomToast(message);
    setTimeout(() => {
      setBottomToast((current) => (current === message ? null : current));
    }, 2500);
  };

  useEffect(() => {
    const handlePopState = (e) => {
      if (e.state?.page === 'about') {
        setCurrentPage('about');
      } else {
        setCurrentPage('main');
      }
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateToAbout = () => {
    window.history.pushState({ page: 'about' }, '', '#about');
    setCurrentPage('about');
  };

  const navigateToMain = () => {
    if (showUpdateModal) {
      setShowUpdateModal(false);
      return;
    }
    if (window.location.hash === '#about') {
      window.history.back();
    } else {
      setCurrentPage('main');
    }
  };

  const toggleHide = () => {
    setIsHidden(!isHidden);
  };

  const handleCopyEmail = (e) => {
    e?.stopPropagation();
    navigator.clipboard.writeText(DEVELOPER_EMAIL);
    showToast(`✓ Copied: ${DEVELOPER_EMAIL}`);
  };

  const handleCopyUpi = () => {
    navigator.clipboard.writeText(UPI_ID);
    showToast(`✓ Copied: ${UPI_ID}`);
  };

  const handleCopyBinance = () => {
    navigator.clipboard.writeText(BINANCE_UID);
    showToast(`✓ Copied: ${BINANCE_UID}`);
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'CleanBar',
          url: GITHUB_REPO_URL,
        });
      } catch (err) {
        // cancelled
      }
    } else {
      navigator.clipboard.writeText(GITHUB_REPO_URL);
      showToast('✓ Link copied');
    }
  };

  const handleCheckUpdate = async () => {
    if (isCheckingUpdate) return;
    setIsCheckingUpdate(true);
    try {
      const res = await fetch('https://api.github.com/repos/sachinmandawi/CleanBar/releases/latest');
      if (res.ok) {
        const data = await res.json();
        const tag = (data.tag_name || '').replace(/^v/i, '');
        const current = APP_VERSION;

        const tParts = tag.split('.').map(Number);
        const cParts = current.split('.').map(Number);
        let hasNewer = false;
        for (let i = 0; i < Math.max(tParts.length, cParts.length); i++) {
          const t = tParts[i] || 0;
          const c = cParts[i] || 0;
          if (t > c) { hasNewer = true; break; }
          if (t < c) break;
        }

        if (hasNewer) {
          let downloadUrl = data.html_url;
          if (data.assets && data.assets.length > 0) {
            const found = data.assets.find(a => a.name.endsWith('.apk'));
            if (found) downloadUrl = found.browser_download_url;
          }
          setUpdateResult({
            version: data.tag_name,
            downloadUrl,
            notes: data.body
          });
          setShowUpdateModal(true);
        } else {
          showToast(`CleanBar is up to date 🎉`);
        }
      } else {
        showToast(`CleanBar is up to date 🎉`);
      }
    } catch (e) {
      showToast(`CleanBar is up to date 🎉`);
    } finally {
      setIsCheckingUpdate(false);
    }
  };

  if (currentPage === 'about') {
    return (
      <div className="min-h-screen bg-[#191919] text-[#EBEBEB] flex flex-col items-center justify-start p-5 sm:p-7 font-sans select-none max-w-md mx-auto relative pb-20">
        
        {/* Floating Bottom Toast Notification */}
        {bottomToast && (
          <div className="fixed bottom-7 z-50 bg-[#252525] border border-[#3E3E3E] text-[#EBEBEB] text-xs font-medium px-4 py-2.5 rounded-full shadow-2xl flex items-center gap-2 animate-in fade-in slide-in-from-bottom-4 duration-200">
            <CheckCircle2 className="w-4 h-4 text-[#4DAB9A]" />
            <span>{bottomToast}</span>
          </div>
        )}

        {/* Update Available Modal */}
        {showUpdateModal && updateResult && (
          <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-[#202020] border border-[#2F2F2F] rounded-3xl p-6 w-full max-w-xs flex flex-col items-center shadow-2xl animate-in fade-in zoom-in-95 duration-200">
              <div className="w-12 h-12 rounded-full bg-[#1E2F3A] border border-[#2B527E] flex items-center justify-center text-[#529CCA] mb-3">
                <Download className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-base text-[#EBEBEB] mb-1">Update Available 🚀</h3>
              <p className="text-xs text-[#9B9A97] text-center mb-4">
                CleanBar {updateResult.version} is ready.
              </p>

              <div className="flex gap-2 w-full">
                <button
                  onClick={() => setShowUpdateModal(false)}
                  className="flex-1 bg-[#191919] border border-[#2F2F2F] hover:border-[#3D3D3D] text-[#9B9A97] text-xs font-medium py-2.5 rounded-xl cursor-pointer transition-all"
                >
                  Later
                </button>
                <a
                  href={updateResult.downloadUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 bg-[#529CCA] hover:bg-[#6CA8E8] text-black text-xs font-bold py-2.5 rounded-xl flex items-center justify-center cursor-pointer transition-all"
                >
                  Download
                </a>
              </div>
            </div>
          </div>
        )}

        {/* Top Header */}
        <div className="w-full flex items-center justify-between pt-4 pb-3">
          <div className="flex items-center gap-3">
            <button
              onClick={navigateToMain}
              className="w-10 h-10 rounded-full bg-[#202020] border border-[#2F2F2F] flex items-center justify-center text-[#EBEBEB] hover:bg-[#2A2A2A] active:scale-95 transition-all cursor-pointer"
              title="Go Back"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h2 className="font-semibold text-base text-[#EBEBEB]">About</h2>
          </div>
          <div className="px-2.5 py-0.5 rounded-full bg-[#15261E] border border-[#224E3A] text-[11px] font-medium text-[#4DAB9A]">
            v{APP_VERSION}
          </div>
        </div>

        {/* Developer Card (Minimal) */}
        <div className="w-full mt-3">
          <div className="w-full bg-[#202020] border border-[#2A2A2A] rounded-2xl p-3 flex items-center gap-3.5">
            <img
              src="/dev_avatar.jpg"
              alt="Sachin Mandawi"
              onError={(e) => {
                e.target.src = 'https://avatars.githubusercontent.com/u/197530811?v=4';
              }}
              className="w-10 h-10 rounded-full object-cover border border-[#3E3E3E] shrink-0"
            />
            <div>
              <div className="font-medium text-sm text-[#EBEBEB]">Sachin Mandawi</div>
              <div className="text-xs text-[#787774]">Developer</div>
            </div>
          </div>
        </div>

        {/* Support Section */}
        <div className="w-full mt-5">
          <div className="text-[11px] font-semibold text-[#787774] uppercase px-1 mb-1.5 tracking-wider">
            Support
          </div>
          <div className="flex flex-col gap-2">
            
            {/* UPI */}
            <div
              onClick={handleCopyUpi}
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#19251E] flex items-center justify-center shrink-0">
                  <UpiLogo className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">UPI • {UPI_ID}</span>
              </div>
              <div className="text-[#787774] hover:text-[#EBEBEB] p-1">
                <Copy className="w-4 h-4" />
              </div>
            </div>

            {/* Binance */}
            <div
              onClick={handleCopyBinance}
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#292416] flex items-center justify-center shrink-0">
                  <BinanceLogo className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">Binance • {BINANCE_UID}</span>
              </div>
              <div className="text-[#787774] hover:text-[#EBEBEB] p-1">
                <Copy className="w-4 h-4" />
              </div>
            </div>

          </div>
        </div>

        {/* Links & Updates */}
        <div className="w-full mt-5">
          <div className="text-[11px] font-semibold text-[#787774] uppercase px-1 mb-1.5 tracking-wider">
            Links
          </div>
          <div className="flex flex-col gap-2">
            
            {/* Check Updates */}
            <button
              onClick={handleCheckUpdate}
              disabled={isCheckingUpdate}
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#18222E] flex items-center justify-center text-[#529CCA] shrink-0">
                  <RefreshCw className={`w-4 h-4 ${isCheckingUpdate ? 'animate-spin' : ''}`} />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">Check for Updates</span>
              </div>
              <span className="text-xs text-[#787774] mr-1">v{APP_VERSION}</span>
            </button>

            {/* Gmail - Copy on left of Launch icon */}
            <div className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between transition-all">
              <div
                onClick={() => {
                  window.location.href = `mailto:${DEVELOPER_EMAIL}?subject=%5BCleanBar%5D%20Feedback`;
                }}
                className="flex items-center gap-3 flex-1 cursor-pointer"
              >
                <div className="w-8 h-8 rounded-lg bg-[#261919] flex items-center justify-center text-[#FF7369] shrink-0">
                  <Mail className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">Gmail</span>
              </div>

              <div className="flex items-center gap-1 shrink-0">
                <button
                  type="button"
                  onClick={handleCopyEmail}
                  className="w-7 h-7 rounded flex items-center justify-center text-[#787774] hover:text-[#EBEBEB] hover:bg-[#2A2A2A] active:scale-95 transition-all cursor-pointer"
                  title="Copy Email"
                >
                  <Copy className="w-4 h-4" />
                </button>
                <a
                  href={`mailto:${DEVELOPER_EMAIL}?subject=%5BCleanBar%5D%20Feedback`}
                  className="w-7 h-7 rounded flex items-center justify-center text-[#787774] hover:text-[#EBEBEB] hover:bg-[#2A2A2A] active:scale-95 transition-all cursor-pointer"
                  title="Open Gmail"
                >
                  <ExternalLink className="w-4 h-4" />
                </a>
              </div>
            </div>

            {/* GitHub */}
            <a
              href={GITHUB_REPO_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#17241F] flex items-center justify-center text-[#4DAB9A] shrink-0">
                  <GithubIcon className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">GitHub</span>
              </div>
              <ExternalLink className="w-4 h-4 text-[#787774] mr-1" />
            </a>

            {/* Report Bug */}
            <a
              href={GITHUB_ISSUES_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#261F17] flex items-center justify-center text-[#FFB86C] shrink-0">
                  <Bug className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">Report Bug</span>
              </div>
              <ExternalLink className="w-4 h-4 text-[#787774] mr-1" />
            </a>

            {/* Share */}
            <button
              onClick={handleShare}
              className="w-full bg-[#202020] border border-[#2A2A2A] hover:border-[#383838] rounded-2xl p-3 flex items-center justify-between cursor-pointer active:scale-[0.99] transition-all"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-[#231926] flex items-center justify-center text-[#C678DD] shrink-0">
                  <Share2 className="w-4 h-4" />
                </div>
                <span className="font-medium text-sm text-[#EBEBEB]">Share App</span>
              </div>
              <Share2 className="w-4 h-4 text-[#787774] mr-1" />
            </button>

          </div>
        </div>

      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#191919] text-[#EBEBEB] flex flex-col items-center justify-between p-6 sm:p-10 font-sans select-none max-w-md mx-auto">
      
      {/* Top Header Row with Shizuku Status Pill & 3-Dot Overflow Menu */}
      <div className="w-full flex items-center justify-between pt-6">
        <div className="w-9" /> {/* Spacer for centering the pill */}

        <div className="flex items-center gap-2 bg-[#202020] border border-[#2F2F2F] px-3.5 py-1.5 rounded-full text-xs text-[#9B9A97] font-medium cursor-pointer hover:bg-[#252525] transition-colors">
          <span className="w-2 h-2 rounded-full bg-[#4DAB9A]" />
          <span className="text-[#EBEBEB]">Shizuku Ready</span>
        </div>

        {/* 3-Dot Menu Button */}
        <button
          onClick={navigateToAbout}
          className="w-9 h-9 rounded-full bg-[#202020] border border-[#2F2F2F] flex items-center justify-center text-[#EBEBEB] hover:bg-[#2A2A2A] active:scale-95 transition-all cursor-pointer"
          title="About Developer & App"
        >
          <MoreVertical className="w-4 h-4" />
        </button>
      </div>

      {/* ========================================================================= */}
      {/* CENTER: FLAT 1-CLICK MASTER BUTTON */}
      {/* ========================================================================= */}
      <div className="flex flex-col items-center space-y-6 my-auto">
        <button
          onClick={toggleHide}
          className={`w-52 h-52 rounded-full border flex flex-col items-center justify-center transition-colors cursor-pointer active:scale-95 ${
            isHidden
              ? 'bg-[#261A1A] border-[#7F2D2D] text-[#FF8585]'
              : 'bg-[#1A2330] border-[#2B4C7E] text-[#6CA8E8]'
          }`}
        >
          {isHidden ? (
            <EyeOff className="w-14 h-14 mb-2" />
          ) : (
            <Eye className="w-14 h-14 mb-2" />
          )}
          <span className="text-xl font-bold tracking-wider">
            {isHidden ? 'HIDDEN' : 'VISIBLE'}
          </span>
        </button>

        <div className="text-center space-y-1">
          <div className={`text-sm font-medium ${isHidden ? 'text-[#FF8585]' : 'text-[#6CA8E8]'}`}>
            {isHidden ? 'Status Bar is 100% HIDDEN' : 'Status Bar is VISIBLE'}
          </div>
          <p className="text-xs text-[#787774]">
            {isHidden
              ? 'Tap button to make Status Bar VISIBLE'
              : 'Tap button to HIDE Status Bar & Clock'}
          </p>
        </div>
      </div>

      {/* Bottom info note */}
      <div className="pb-4 text-center">
        <span className="text-[11px] text-[#787774]">CleanBar v{APP_VERSION}</span>
      </div>

    </div>
  );
}
